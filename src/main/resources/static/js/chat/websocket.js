/**
 * Módulo de conexão e subscrição WebSocket (STOMP).
 */

import { obterTopicoConversa } from './utils.js';
import { adicionarMensagemNoContainer } from './mensagens.js';
import { atualizarConversas } from './conversas.js';
import { getUidUsuarioLogado } from './state.js';

let stompClient = null;
let clienteAtual = null;
let subscricaoAtiva = null;
let conversaAtiva = null;
let callbackStatusWebSocket = null;
let temporizadorReconexao = null;
let promessaConexao = null;

function notificarStatusWebSocket(conectado) {
    if (typeof callbackStatusWebSocket !== 'function') return;

    try {
        callbackStatusWebSocket(conectado);
    } catch (erro) {
        console.error('Erro ao atualizar o estado do fallback do chat:', erro);
    }
}

function programarReconexao() {
    if (temporizadorReconexao || !getUidUsuarioLogado()) return;

    temporizadorReconexao = window.setTimeout(() => {
        temporizadorReconexao = null;
        conectarWebSocket().catch(erro => {
            console.warn('Falha ao reconectar WebSocket; o polling permanece ativo:', erro);
        });
    }, 5000);
}

function tratarDesconexao(cliente) {
    if (clienteAtual !== cliente) return;

    clienteAtual = null;
    stompClient = null;
    subscricaoAtiva = null;
    notificarStatusWebSocket(false);
    programarReconexao();
}

function subscreverConversaAtiva() {
    if (!conversaAtiva || !stompClient || !stompClient.connected) {
        return false;
    }

    const topico = obterTopicoConversa(
        conversaAtiva.uidAnuncio,
        conversaAtiva.uidRemetente,
        conversaAtiva.uidDestinatario
    );

    try {
        if (subscricaoAtiva) {
            subscricaoAtiva.unsubscribe();
            subscricaoAtiva = null;
        }

        subscricaoAtiva = stompClient.subscribe(topico, payload => {
            let evento;
            try {
                evento = JSON.parse(payload.body);
            } catch (erro) {
                console.error('Não foi possível interpretar o evento WebSocket como JSON:', erro, payload.body);
                return;
            }

            if (!evento || typeof evento !== 'object') {
                console.error('Evento WebSocket recebido em formato inválido:', evento);
                return;
            }

            console.log('Evento WebSocket recebido no front:', evento);

            const tipo = evento.tipo || (evento.mensagem ? 'NOVA_MENSAGEM' : '');

            if (tipo === 'MENSAGEM_EDITADA' || tipo === 'EDITADA') {
                const msgObj = evento.mensagem || evento;
                const msgId = msgObj.id || msgObj._id;
                const p = document.querySelector(`[data-id="${msgId}"] p`);
                if (p && typeof msgObj.conteudo === 'string') {
                    p.textContent = msgObj.conteudo;
                }
            } else if (tipo === 'MENSAGEM_EXCLUIDA' || tipo === 'EXCLUIDA') {
                const msgId = evento.idMensagemExcluida
                    || evento.idExcluido
                    || (evento.mensagem && (evento.mensagem.id || evento.mensagem._id));
                const div = document.querySelector(`[data-id="${msgId}"]`);
                if (div) {
                    div.remove();
                }
            } else {
                const msg = evento.mensagem || evento;
                if (msg && typeof msg.conteudo === 'string') {
                    adicionarMensagemNoContainer(msg);
                } else {
                    console.warn('Evento WebSocket não contém uma mensagem com campo "conteudo":', evento);
                }
            }

            atualizarConversas();
        });
        console.log(`Subscrito no tópico: ${topico}`);
        return true;
    } catch (erro) {
        console.error(`Não foi possível subscrever ao tópico ${topico}:`, erro);
        subscricaoAtiva = null;
        return false;
    }
}

/**
 * Inicializa a conexão WebSocket/STOMP com o microsserviço (porta 8282).
 */
export function conectarWebSocket() {
    if (stompClient && stompClient.connected) {
        return Promise.resolve(stompClient);
    }
    if (promessaConexao) {
        return promessaConexao;
    }

    const chatServiceUrl = window.CHAT_SERVICE_URL || 'http://localhost:8282';
    const chatServiceFallbackUrl = window.CHAT_SERVICE_FALLBACK_URL || 'http://localhost:8282';

    const conexao = new Promise((resolve, reject) => {
        const tentarConectar = (url, isFallback = false) => {
            console.log(`Tentando conectar ao WebSocket em: ${url}`);

            let socket;
            let client;
            try {
                socket = new SockJS(`${url}/ws-chat`);
                client = Stomp.over(socket);
            } catch (erro) {
                if (!isFallback && chatServiceUrl !== chatServiceFallbackUrl) {
                    tentarConectar(chatServiceFallbackUrl, true);
                } else {
                    notificarStatusWebSocket(false);
                    programarReconexao();
                    reject(erro);
                }
                return;
            }

            clienteAtual = client;
            let conectado = false;
            const headers = { uidUsuario: getUidUsuarioLogado() };

            const tratarErro = erro => {
                if (clienteAtual !== client) return;

                if (!conectado && !isFallback && chatServiceUrl !== chatServiceFallbackUrl) {
                    tentarConectar(chatServiceFallbackUrl, true);
                    return;
                }

                tratarDesconexao(client);
                reject(erro);
            };

            client.debug = str => {
                if (
                    str.includes('MESSAGE') ||
                    str.includes('SUBSCRIBE') ||
                    str.includes('CONNECT') ||
                    str.includes('ERROR')
                ) {
                    console.log('[STOMP]', str);
                }
            };

            client.connect(headers, () => {
                if (clienteAtual !== client) return;

                conectado = true;
                stompClient = client;
                if (temporizadorReconexao) {
                    window.clearTimeout(temporizadorReconexao);
                    temporizadorReconexao = null;
                }

                const inscrito = conversaAtiva ? subscreverConversaAtiva() : true;
                notificarStatusWebSocket(Boolean(client.connected && inscrito));
                console.log(`Conectado ao WebSocket com sucesso em: ${url}`);
                resolve(client);
            }, tratarErro);

            const oncloseOriginal = socket.onclose;
            socket.onclose = evento => {
                try {
                    if (typeof oncloseOriginal === 'function') {
                        oncloseOriginal.call(socket, evento);
                    }
                } finally {
                    tratarDesconexao(client);
                }
            };
        };

        tentarConectar(chatServiceUrl);
    });

    promessaConexao = conexao;
    conexao.then(
        () => {
            if (promessaConexao === conexao) promessaConexao = null;
        },
        () => {
            if (promessaConexao === conexao) promessaConexao = null;
        }
    );
    return conexao;
}

export function definirCallbackStatusWebSocket(callback) {
    callbackStatusWebSocket = callback;
    notificarStatusWebSocket(Boolean(stompClient && stompClient.connected));
}

/**
 * Cancela o tópico antigo e subscreve no novo tópico da conversa aberta.
 */
export function subscreverTopicoConversa(uidAnuncio, uidRemetente, uidDestinatario) {
    conversaAtiva = { uidAnuncio, uidRemetente, uidDestinatario };
    return subscreverConversaAtiva();
}
