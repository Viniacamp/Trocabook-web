/**
 * Ponto de entrada do Chat.
 * Coordena inicialização, abertura de conversas e integração dos módulos.
 */

import { getMessagingSeguro } from '../firebase.js';
import {
    conectarWebSocket,
    definirCallbackStatusWebSocket,
    subscreverTopicoConversa
} from './websocket.js';
import { onMessage } from 'https://www.gstatic.com/firebasejs/12.18.0/firebase-messaging.js';

import {
    elements,
    mostrarCarregamentoConversa,
    exibirErroConversa,
    atualizarUsuarioHeader,
    atualizarLivroInfo,
    destacarConversaAtual
} from './ui.js';
import {
    setUidUsuarioLogado,
    setConversaAtual,
    setAssinaturaMensagens,
    getConversaAtual, getUidUsuarioLogado
} from './state.js';
import {
    carregarDadosConversa
} from './api.js';
import {
    renderizarMensagens,
    gerarAssinaturaMensagens,
    enviarMensagem,
    iniciarAtualizacaoMensagens,
    pararAtualizacaoMensagens,
    atualizarMensagens,
    configurarModaisMensagem
} from './mensagens.js';
import {
    iniciarAtualizacaoConversas,
    pararAtualizacaoConversas,
    filtrarConversas,
    atualizarConversas
} from './conversas.js';
import {
    renderizarNegociacao,
    configurarModaisNegociacao
} from './negociacao.js';

export async function abrirConversa(uidAnuncio, uidDestinatario, elemento = null) {
    try {
        pararAtualizacaoMensagens();

        setConversaAtual(uidAnuncio, uidDestinatario);
        setAssinaturaMensagens('');

        /* ---------------------------------------------
           Destaca conversa selecionada
           --------------------------------------------- */
        document.querySelectorAll('.chat-item').forEach(item => {
            item.classList.remove('active');
        });

        if (elemento) {
            elemento.classList.add('active');
        }

        /* ---------------------------------------------
           Mostra carregamento
           --------------------------------------------- */
        mostrarCarregamentoConversa(uidAnuncio, uidDestinatario);

        /* ---------------------------------------------
           Busca dados da conversa
           --------------------------------------------- */
        const conversa = await carregarDadosConversa(uidAnuncio, uidDestinatario);

        setConversaAtual(
            uidAnuncio,
            uidDestinatario,
            conversa.anuncio,
            conversa.negociacao || null
        );

        /* ---------------------------------------------
           Dados do usuário
           --------------------------------------------- */
        if (conversa.usuarioNegociante) {
            atualizarUsuarioHeader(conversa.usuarioNegociante);
        }

        /* ---------------------------------------------
           Dados do livro
           --------------------------------------------- */
        if (conversa.anuncio) {
            atualizarLivroInfo(conversa.anuncio);
        }

        /* ---------------------------------------------
           Negociação
           --------------------------------------------- */
        renderizarNegociacao(
            conversa.anuncio,
            conversa.negociacao
        );

        /* ---------------------------------------------
           Mensagens
           --------------------------------------------- */
        const mensagens = conversa.mensagens || [];
        renderizarMensagens(mensagens);
        setAssinaturaMensagens(gerarAssinaturaMensagens(mensagens));

        /* ---------------------------------------------
           Inicia atualização automática das mensagens
           --------------------------------------------- */
        const inscritoNoWebSocket = subscreverTopicoConversa(
            uidAnuncio,
            getUidUsuarioLogado(),
            uidDestinatario
        );
        if (inscritoNoWebSocket) {
            pararAtualizacaoMensagens();
        } else {
            iniciarAtualizacaoMensagens((anuncioAtualizado, negociacaoAtualizada) => {
                renderizarNegociacao(anuncioAtualizado, negociacaoAtualizada);
            });
        }

        destacarConversaAtual(uidAnuncio, uidDestinatario);

    } catch (erro) {
        console.error('Erro ao abrir conversa:', erro);
        exibirErroConversa();
    }
}

function inicializarEventos() {
    // Form de envio de mensagem
    if (elements.chatForm) {
        elements.chatForm.addEventListener('submit', enviarMensagem);
    }

    // Campo de busca de conversas na barra lateral
    if (elements.pesquisaConversas) {
        elements.pesquisaConversas.addEventListener('input', function () {
            filtrarConversas(this.value);
        });
    }

    // Clique nas conversas iniciais da barra lateral
    document.querySelectorAll('.chat-item').forEach(item => {
        item.addEventListener('click', function () {
            const uidAnuncio = this.dataset.anuncio;
            const uidDestinatario = this.dataset.destinatario;
            abrirConversa(uidAnuncio, uidDestinatario, this);
        });
    });

    // Configuração dos modais de negociação
    configurarModaisNegociacao();

    // Configuração dos modais de edição e exclusão de mensagem
    configurarModaisMensagem();

    // Finaliza polling ao sair da página
    window.addEventListener('beforeunload', () => {
        pararAtualizacaoMensagens();
        pararAtualizacaoConversas();
    });
}


async function configurarNotificacoesEmPrimeiroPlano() {
    try {
        const messaging = await getMessagingSeguro();
        if (!messaging) return;

        onMessage(messaging, (payload) => {
            console.log("Notificação Push recebida em primeiro plano:", payload);

            // Atualiza sempre a lista de conversas da barra lateral
            atualizarConversas((uidAnuncio, uidDestinatario, elemento) => {
                abrirConversa(uidAnuncio, uidDestinatario, elemento);
            });

            // Se a conversa aberta for a mesma da notificação, força atualização imediata das mensagens
            const conversaAtual = getConversaAtual();
            const dados = payload.data || {};

            if (
                conversaAtual.uidAnuncio === dados.anuncio &&
                conversaAtual.uidDestinatario === dados.destinatario
            ) {
                atualizarMensagens((anuncioAtualizado, negociacaoAtualizada) => {
                    renderizarNegociacao(anuncioAtualizado, negociacaoAtualizada);
                });
            }
        });
    } catch (error) {
        console.warn("Não foi possível configurar ouvinte Push em primeiro plano:", error);
    }
}

export async function inicializarChat() {
    // Define o usuário logado
    const uidUsuario = elements.uidRemetenteInput ? elements.uidRemetenteInput.value : '';
    setUidUsuarioLogado(uidUsuario);

    // Inicializa eventos da UI e modais
    inicializarEventos();

    // Configura Push em primeiro plano
    configurarNotificacoesEmPrimeiroPlano();

    definirCallbackStatusWebSocket(conectado => {
        const conversaAtual = getConversaAtual();
        if (!conversaAtual.uidAnuncio || !conversaAtual.uidDestinatario) return;

        if (conectado) {
            pararAtualizacaoMensagens();
        } else {
            iniciarAtualizacaoMensagens((anuncioAtualizado, negociacaoAtualizada) => {
                renderizarNegociacao(anuncioAtualizado, negociacaoAtualizada);
            });
        }
    });

    conectarWebSocket().catch(erro => {
        console.warn("Falha ao conectar WebSocket; o polling será usado como fallback.", erro);
    });

    // Inicia o polling da lista de conversas
    await atualizarConversas((uidAnuncio, uidDestinatario, elemento) => {
        abrirConversa(uidAnuncio, uidDestinatario, elemento);
    });
    iniciarAtualizacaoConversas((uidAnuncio, uidDestinatario, elemento) => {
        abrirConversa(uidAnuncio, uidDestinatario, elemento);
    });

    // Abre uma conversa automaticamente quando o usuário veio com parâmetros iniciais
    const anuncioInicial = document.getElementById('anuncioInicial')?.value || null;
    const destinatarioInicial = document.getElementById('destinatarioInicial')?.value || null;

    if (anuncioInicial && destinatarioInicial) {
        abrirConversa(anuncioInicial, destinatarioInicial);
    }
}

// Inicializa quando o DOM estiver pronto
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarChat);
} else {
    inicializarChat();
}