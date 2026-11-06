/**
 * Módulo de conexão e subscrição WebSocket (STOMP).
 */

import { obterTopicoConversa } from './utils.js';
import { adicionarMensagemNoContainer } from './mensagens.js';
import { atualizarConversas } from './conversas.js';

let stompClient = null;
let subscricaoAtiva = null;

/**
 * Inicializa a conexão WebSocket/STOMP com o microsserviço (porta 8282).
 */
export function conectarWebSocket() {
    return new Promise((resolve, reject) => {
        const chatServiceUrl = window.CHAT_SERVICE_URL || 'http://localhost:8282';

        const socket = new SockJS(`${chatServiceUrl}/ws-chat`);
        stompClient = Stomp.over(socket);

        stompClient.debug = null;

        stompClient.connect({}, () => {
            console.log("Conectado ao WebSocket com sucesso.");
            resolve(stompClient);
        }, (error) => {
            console.error("Erro na conexão WebSocket:", error);
            reject(error);
        });
    });
}

/**
 * Cancela o tópico antigo e subscreve no novo tópico da conversa aberta.
 */
export function subscreverTopicoConversa(uidAnuncio, uidRemetente, uidDestinatario) {
    if (!stompClient || !stompClient.connected) {
        console.warn("WebSocket não está conectado.");
        return;
    }

    // 1. Cancela a escuta da conversa anterior
    if (subscricaoAtiva) {
        subscricaoAtiva.unsubscribe();
        subscricaoAtiva = null;
    }

    // 2. Monta o nome do tópico
    const topico = obterTopicoConversa(uidAnuncio, uidRemetente, uidDestinatario);

    // 3. Subscreve no tópico da conversa atual
    subscricaoAtiva = stompClient.subscribe(topico, (payload) => {
        const evento = JSON.parse(payload.body);

        if (evento.tipo === 'MENSAGEM_EDITADA') {
            const msgObj = evento.mensagem || {};
            const msgId = msgObj.id || msgObj._id;
            const p = document.querySelector(`[data-id="${msgId}"] p`);
            if (p && msgObj.conteudo) {
                p.textContent = msgObj.conteudo;
            }
        } else if (evento.tipo === 'MENSAGEM_EXCLUIDA') {
            const msgId = evento.idExcluido || (evento.mensagem && (evento.mensagem.id || evento.mensagem._id));
            const div = document.querySelector(`[data-id="${msgId}"]`);
            if (div) {
                div.remove();
            }
        } else {
            // Nova mensagem recebida
            const msg = evento.mensagem || evento;
            adicionarMensagemNoContainer(msg);
        }

        // Atualiza a prévia da conversa na barra lateral
        atualizarConversas();
    });

    console.log(`Subscrito no tópico: ${topico}`);
}