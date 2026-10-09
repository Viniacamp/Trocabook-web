/**
 * Ponto de entrada do Chat.
 * Coordena inicialização, abertura de conversas e integração dos módulos.
 */

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
    setAssinaturaMensagens
} from './state.js';
import {
    carregarDadosConversa
} from './api.js';
import {
    renderizarMensagens,
    gerarAssinaturaMensagens,
    enviarMensagem,
    iniciarAtualizacaoMensagens,
    pararAtualizacaoMensagens
} from './mensagens.js';
import {
    iniciarAtualizacaoConversas,
    pararAtualizacaoConversas,
    filtrarConversas
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
        iniciarAtualizacaoMensagens((anuncioAtualizado, negociacaoAtualizada) => {
            renderizarNegociacao(anuncioAtualizado, negociacaoAtualizada);
        });

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

    // Finaliza polling ao sair da página
    window.addEventListener('beforeunload', () => {
        pararAtualizacaoMensagens();
        pararAtualizacaoConversas();
    });
}

export function inicializarChat() {
    // Define o usuário logado
    const uidUsuario = elements.uidRemetenteInput ? elements.uidRemetenteInput.value : '';
    setUidUsuarioLogado(uidUsuario);

    // Inicializa eventos da UI e modais
    inicializarEventos();

    // Inicia o polling da lista de conversas
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
