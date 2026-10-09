/**
 * Estado compartilhado da aplicação de Chat.
 */

const state = {
    uidUsuarioLogado: null,
    conversaAtual: {
        uidAnuncio: null,
        uidDestinatario: null,
        anuncio: null,
        negociacao: null
    },
    anuncioOferecidoSelecionado: null,
    assinaturaMensagens: '',
    intervaloMensagens: null,
    intervaloConversas: null,
    assinaturaConversas: ''
};

export function getUidUsuarioLogado() {
    return state.uidUsuarioLogado;
}

export function setUidUsuarioLogado(uid) {
    state.uidUsuarioLogado = uid;
}

export function getConversaAtual() {
    return state.conversaAtual;
}

export function setConversaAtual(uidAnuncio, uidDestinatario, anuncio = null, negociacao = null) {
    state.conversaAtual.uidAnuncio = uidAnuncio;
    state.conversaAtual.uidDestinatario = uidDestinatario;
    state.conversaAtual.anuncio = anuncio;
    state.conversaAtual.negociacao = negociacao;
}

export function setConversaAtualAnuncio(anuncio) {
    state.conversaAtual.anuncio = anuncio;
}

export function setConversaAtualNegociacao(negociacao) {
    state.conversaAtual.negociacao = negociacao;
}

export function getAnuncioOferecidoSelecionado() {
    return state.anuncioOferecidoSelecionado;
}

export function setAnuncioOferecidoSelecionado(id) {
    state.anuncioOferecidoSelecionado = id;
}

export function getAssinaturaMensagens() {
    return state.assinaturaMensagens;
}

export function setAssinaturaMensagens(assinatura) {
    state.assinaturaMensagens = assinatura;
}

export function getIntervaloMensagens() {
    return state.intervaloMensagens;
}

export function setIntervaloMensagens(timer) {
    state.intervaloMensagens = timer;
}

export function clearIntervaloMensagens() {
    if (state.intervaloMensagens) {
        clearInterval(state.intervaloMensagens);
        state.intervaloMensagens = null;
    }
}

export function getIntervaloConversas() {
    return state.intervaloConversas;
}

export function setIntervaloConversas(timer) {
    state.intervaloConversas = timer;
}

export function clearIntervaloConversas() {
    if (state.intervaloConversas) {
        clearInterval(state.intervaloConversas);
        state.intervaloConversas = null;
    }
}

export function getAssinaturaConversas() {
    return state.assinaturaConversas;
}

export function setAssinaturaConversas(assinatura) {
    state.assinaturaConversas = assinatura;
}

export default state;
