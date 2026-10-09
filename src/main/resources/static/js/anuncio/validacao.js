/**
 * Módulo responsável pela validação e submissão do formulário de anúncio.
 */

import { dom } from './ui.js';
import { getAutoresAutomaticos, getCategoriasAutomaticas } from './state.js';
import { criarInputsManuais } from './autores.js';
import { criarInputsCategoriasSelecionadas } from './categorias.js';

export function validarFormulario(event) {
    const modoCadastro = dom.modoCadastro();
    const modo = modoCadastro ? modoCadastro.value : "";

    if (modo === "GOOGLE_BOOKS") {
        if (getAutoresAutomaticos().length === 0) {
            event.preventDefault();
            window.alert("Informe pelo menos um autor.");
            return;
        }

        if (getCategoriasAutomaticas().length === 0) {
            event.preventDefault();
            window.alert("Selecione pelo menos uma categoria.");
            return;
        }

        return;
    }

    if (modo !== "MANUAL") {
        return;
    }

    criarInputsManuais(
        "autoresManual",
        "autoresManuaisHidden",
        "autores"
    );

    criarInputsCategoriasSelecionadas();

    const tituloInput = dom.titulo();
    const titulo = tituloInput ? tituloInput.value.trim() : "";

    if (titulo.length === 0) {
        event.preventDefault();
        window.alert("Informe o título do livro.");
        return;
    }

    const inputImagem = dom.inputImagem();
    if (!inputImagem || !inputImagem.files || inputImagem.files.length === 0) {
        event.preventDefault();
        window.alert("Selecione uma capa para o livro.");
        return;
    }

    const autoresHidden = dom.autoresManuaisHidden();
    if (!autoresHidden || autoresHidden.children.length === 0) {
        event.preventDefault();
        window.alert("Informe pelo menos um autor.");
        return;
    }

    const categoriasHidden = dom.categoriasManuaisHidden();
    if (!categoriasHidden || categoriasHidden.children.length === 0) {
        event.preventDefault();
        window.alert("Selecione pelo menos uma categoria.");
        return;
    }
}

export function inicializarValidacao() {
    const formAnuncio = dom.formAnuncio();

    if (formAnuncio) {
        formAnuncio.addEventListener("submit", validarFormulario);
    }
}
