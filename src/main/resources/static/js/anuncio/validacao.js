/**
 * Módulo responsável pela validação e submissão do formulário de anúncio.
 */

import { dom, getCsrfToken } from './ui.js';
import { getAutoresAutomaticos, getCategoriasAutomaticas, limparDadosManuais } from './state.js';
import { limparDadosLivro } from "./busca.js";
import { limparCategoriasManuais } from "./categorias.js";
import { limparPreviewCapa } from "./imagem.js";
import { criarInputsManuais } from './autores.js';
import { criarInputsCategoriasSelecionadas } from './categorias.js';

export function limparErrosFormulario() {
    const erros = [
        dom.erroFoto(),
        dom.erroTitulo(),
        dom.erroAutor(),
        dom.erroCategoriaAuto(),
        dom.erroCategoriaManual(),
        dom.erroTipo()
    ];

    erros.forEach(el => {
        if (el) {
            el.textContent = "";
            el.style.display = "none";
        }
    });
}

export function exibirErrosServidor(errosMap) {
    limparErrosFormulario();

    if (errosMap.erroFoto && dom.erroFoto()) {
        dom.erroFoto().textContent = errosMap.erroFoto;
        dom.erroFoto().style.display = "block";
    }
    if (errosMap.erroTitulo && dom.erroTitulo()) {
        dom.erroTitulo().textContent = errosMap.erroTitulo;
        dom.erroTitulo().style.display = "block";
    }
    if (errosMap.erroAutor && dom.erroAutor()) {
        dom.erroAutor().textContent = errosMap.erroAutor;
        dom.erroAutor().style.display = "block";
    }
    if (errosMap.erroCategoria) {
        if (dom.erroCategoriaAuto()) {
            dom.erroCategoriaAuto().textContent = errosMap.erroCategoria;
            dom.erroCategoriaAuto().style.display = "block";
        }
        if (dom.erroCategoriaManual()) {
            dom.erroCategoriaManual().textContent = errosMap.erroCategoria;
            dom.erroCategoriaManual().style.display = "block";
        }
    }
    if (errosMap.erroTipo && dom.erroTipo()) {
        dom.erroTipo().textContent = errosMap.erroTipo;
        dom.erroTipo().style.display = "block";
    }
}

export async function submeterFormulario(event) {
    event.preventDefault();
    limparErrosFormulario();

    const modoCadastro = dom.modoCadastro();
    const modo = modoCadastro ? modoCadastro.value : "";

    if (modo === "GOOGLE_BOOKS") {
        if (getAutoresAutomaticos().length === 0) {
            window.alert("Informe pelo menos um autor.");
            return;
        }
        if (getCategoriasAutomaticas().length === 0) {
            window.alert("Selecione pelo menos uma categoria.");
            return;
        }
    } else if (modo === "MANUAL") {
        criarInputsManuais("autoresManual", "autoresManuaisHidden", "autores");
        criarInputsCategoriasSelecionadas();
    }

    // Exibe o modal e altera o texto dinamicamente
    const modalLoading = dom.modalLoading();
    if (modalLoading) {
        const textoModal = modalLoading.querySelector("p");
        if (textoModal) {
            textoModal.innerHTML = "Por favor aguarde um momento<br>enquanto o anúncio é criado";
        }
        modalLoading.style.display = "flex";
    }

    const formAnuncio = dom.formAnuncio();
    const formData = new FormData(formAnuncio);
    const csrfToken = getCsrfToken();

    try {
        const response = await fetch("/AnunciarLivro", {
            method: "POST",
            headers: {
                "X-CSRF-TOKEN": csrfToken
            },
            body: formData
        });

        // Oculta o modal assim que o servidor responder
        if (modalLoading) {
            modalLoading.style.display = "none";
        }

        if (response.status === 201) {

            limpezaGeral();

            if (formAnuncio) {
                formAnuncio.reset();
            }

            // Sucesso: Exibe o Snackbar flutuante
            const snackbar = dom.snackbarAnuncio();
            if (snackbar) {
                snackbar.style.display = "flex";
                setTimeout(() => {
                    snackbar.classList.add("show");
                }, 10);

                setTimeout(() => {
                    snackbar.classList.remove("show");
                    setTimeout(() => {
                        snackbar.style.display = "none";
                    }, 300);
                }, 4000);
            }
            return;
        }

        if (response.status === 400) {
            const erros = await response.json();
            exibirErrosServidor(erros);
            return;
        }

        if (response.status === 401) {
            window.location.href = "/";
            return;
        }

        window.alert("Erro inesperado ao criar o anúncio. Tente novamente.");

    } catch (err) {
        if (modalLoading) {
            modalLoading.style.display = "none";
        }
        console.error("Erro na requisição de anúncio:", err);
        window.alert("Falha de conexão com o servidor.");
    }
}

export function inicializarValidacao() {
    const formAnuncio = dom.formAnuncio();
    if (formAnuncio) {
        formAnuncio.addEventListener("submit", submeterFormulario);
    }
}


export function limpezaGeral(){
    const modoAutomatico = dom.modoAutomatico();
    const modoManual = dom.modoManual();
    const modoCadastro = dom.modoCadastro();
    const buscaAutomatica = dom.buscaAutomatica();
    const camposAnuncio = dom.camposAnuncio();

    if (modoCadastro) modoCadastro.value = "";

    if (modoAutomatico) modoAutomatico.classList.remove("selecionado");
    if (modoManual) modoManual.classList.remove("selecionado");

    if (buscaAutomatica) buscaAutomatica.style.display = "none";

    if (camposAnuncio) camposAnuncio.style.display = "none";

    limparErrosFormulario()
    limparPreviewCapa()
    limparDadosLivro()
    limparCategoriasManuais()
    limparDadosManuais()


}