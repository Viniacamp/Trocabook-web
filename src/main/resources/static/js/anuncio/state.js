/**
 * Estado compartilhado da página de anúncio de livro.
 */

const state = {
    categoriasConfirmadas: [],
    autoresAutomaticos: [],
    categoriasAutomaticas: [],
    modoSelecaoCategorias: "MANUAL"
};

export function getCategoriasConfirmadas() {
    return state.categoriasConfirmadas;
}

export function setCategoriasConfirmadas(categorias) {
    state.categoriasConfirmadas = Array.isArray(categorias) ? [...categorias] : [];
}

export function removerCategoriaConfirmada(nome) {
    state.categoriasConfirmadas = state.categoriasConfirmadas.filter(
        categoria => categoria !== nome
    );
}

export function getAutoresAutomaticos() {
    return state.autoresAutomaticos;
}

export function setAutoresAutomaticos(autores) {
    state.autoresAutomaticos = Array.isArray(autores) ? [...autores] : [];
}

export function adicionarAutorAutomatico(autor) {
    state.autoresAutomaticos.push(autor);
}

export function removerAutorAutomatico(nome) {
    state.autoresAutomaticos = state.autoresAutomaticos.filter(
        item => item !== nome
    );
}

export function getCategoriasAutomaticas() {
    return state.categoriasAutomaticas;
}

export function setCategoriasAutomaticas(categorias) {
    state.categoriasAutomaticas = Array.isArray(categorias) ? [...categorias] : [];
}

export function removerCategoriaAutomatica(nome) {
    state.categoriasAutomaticas = state.categoriasAutomaticas.filter(
        item => item !== nome
    );
}

export function getModoSelecaoCategorias() {
    return state.modoSelecaoCategorias;
}

export function setModoSelecaoCategorias(modo) {
    state.modoSelecaoCategorias = modo;
}

export function limparDadosAutomaticos() {
    state.autoresAutomaticos = [];
    state.categoriasAutomaticas = [];
}

export function limparDadosManuais() {
    state.categoriasConfirmadas = [];
}

export default state;
