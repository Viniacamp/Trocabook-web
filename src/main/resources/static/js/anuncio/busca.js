/**
 * Módulo responsável pela busca automática de livros via Google Books e preenchimento dos campos.
 */

import { dom, getCsrfToken } from './ui.js';
import {
    setAutoresAutomaticos,
    setCategoriasAutomaticas,
    limparDadosAutomaticos
} from './state.js';
import { atualizarAutoresAutomaticos } from './autores.js';
import { atualizarCategoriasAutomaticas } from './categorias.js';

export function preencherCampos(livro) {
    const titulo = dom.titulo();
    const capaLivro = dom.capaLivro();
    const urlImagem = dom.urlImagem();
    const dataPublicacao = dom.dataPublicacao();
    const lingua = dom.lingua();
    const googleBooksId = dom.googleBooksId();
    const camposAnuncio = dom.camposAnuncio();

    if (titulo) titulo.value = livro.titulo || "";
    if (capaLivro) {
        capaLivro.src = livro.urlImagem || "";
        capaLivro.style.display = "block";
    }
    if (urlImagem) urlImagem.value = livro.urlImagem || "";
    if (dataPublicacao) dataPublicacao.value = livro.dataPublicacao || "";
    if (lingua) lingua.value = livro.lingua || "en";
    if (googleBooksId) googleBooksId.value = livro.googleBooksId || "";

    const autores = Array.isArray(livro.autores)
        ? livro.autores
            .filter(autor => autor && autor.trim())
            .map(autor => autor.trim())
        : [];

    const categorias = Array.isArray(livro.categorias)
        ? livro.categorias
            .filter(categoria => categoria && categoria.trim())
            .map(categoria => categoria.trim())
        : [];

    setAutoresAutomaticos(autores);
    setCategoriasAutomaticas(categorias);

    atualizarAutoresAutomaticos();
    atualizarCategoriasAutomaticas();

    if (camposAnuncio) {
        camposAnuncio.style.display = "block";
    }
}

export function limparDadosLivro() {
    const titulo = dom.titulo();
    const dataPublicacao = dom.dataPublicacao();
    const lingua = dom.lingua();
    const googleBooksId = dom.googleBooksId();
    const urlImagem = dom.urlImagem();
    const capaLivro = dom.capaLivro();
    const autoresDiv = dom.autoresDiv();
    const categoriasDiv = dom.categoriasDiv();
    const novoAutor = dom.novoAutorAutomatico();
    const resultados = dom.resultadosBusca();

    if (titulo) titulo.value = "";
    if (dataPublicacao) dataPublicacao.value = "";
    if (lingua) lingua.value = "";
    if (googleBooksId) googleBooksId.value = "";
    if (urlImagem) urlImagem.value = "";

    if (capaLivro) {
        capaLivro.src = "";
        capaLivro.style.display = "none";
    }

    limparDadosAutomaticos();

    if (autoresDiv) autoresDiv.innerHTML = "";
    if (categoriasDiv) categoriasDiv.innerHTML = "";
    if (novoAutor) novoAutor.value = "";
    if (resultados) resultados.innerHTML = "";
}

export function buscarLivro() {
    const inputBusca = dom.inputBusca();
    const titulo = inputBusca ? inputBusca.value : "";
    const csrfToken = getCsrfToken();
    const divResultados = dom.resultadosBusca();

    if (!divResultados) return;

    divResultados.innerHTML = "";
    divResultados.style.display = "block";

    if (titulo.trim().length === 0) {
        window.alert("Insira um titulo");
        return;
    }

    window.alert("Iniciando Busca, isso pode demorar um pouco aguarde...");

    fetch("/buscar", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify({ titulo })
    })
        .then(res => {
            if (!res.ok) throw new Error("Erro ao buscar livro");
            return res.json();
        })
        .then(livros => {
            if (!livros || livros.length === 0) {
                divResultados.innerHTML = "<p>Nenhum livro encontrado 😥</p>";
                return;
            }

            livros.forEach(livro => {
                const link = document.createElement("a");
                link.href = "#";
                link.addEventListener("click", e => {
                    e.preventDefault();
                    preencherCampos(livro);
                    divResultados.innerHTML = "";
                });

                const capa = document.createElement("img");
                capa.src = livro.urlImagem || "/img/default-capa.png";
                capa.className = "livro-capa";

                const texto = document.createElement("p");
                texto.innerText = livro.titulo;

                link.appendChild(capa);
                link.appendChild(texto);
                divResultados.appendChild(link);
            });
        })
        .catch(err => {
            divResultados.innerHTML = `<p>Erro: ${err.message}</p>`;
        });
}

export function inicializarBusca() {
    const botaoBuscar = dom.botaoBuscar();
    const inputBusca = dom.inputBusca();

    if (botaoBuscar) {
        botaoBuscar.addEventListener("click", function (event) {
            event.stopPropagation();
            buscarLivro();
        });
    }

    if (inputBusca) {
        inputBusca.addEventListener("focus", function () {
            const resultados = dom.resultadosBusca();
            if (resultados && resultados.innerHTML.trim() !== "") {
                resultados.style.display = "block";
            }
        });
    }

    document.addEventListener("click", function (event) {
        const container = dom.containerBusca();
        const resultados = dom.resultadosBusca();
        const botao = dom.botaoBuscar();

        if (
            container &&
            resultados &&
            !container.contains(event.target) &&
            event.target !== botao
        ) {
            resultados.style.display = "none";
        }
    });
}
