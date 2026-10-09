/**
 * Módulo responsável pela alternância entre os modos Automático (Google Books) e Manual.
 */

import { dom } from './ui.js';
import { limparPreviewCapa } from './imagem.js';
import { limparCategoriasManuais } from './categorias.js';
import { limparDadosLivro } from './busca.js';

export function selecionarModoAutomatico() {
    const autoresManual = dom.autoresManual();
    const autoresManuaisHidden = dom.autoresManuaisHidden();
    const categoriasManuaisHidden = dom.categoriasManuaisHidden();
    const modoCadastro = dom.modoCadastro();
    const modoAutomatico = dom.modoAutomatico();
    const modoManual = dom.modoManual();
    const buscaAutomatica = dom.buscaAutomatica();
    const camposAnuncio = dom.camposAnuncio();
    const capaAutomatica = dom.capaAutomatica();
    const capaManual = dom.capaManual();
    const titulo = dom.titulo();
    const dataPublicacaoContainer = dom.dataPublicacaoContainer();
    const autoresAutomaticos = dom.autoresAutomaticos();
    const autoresManuais = dom.autoresManuais();
    const categoriasAutomaticas = dom.categoriasAutomaticas();
    const categoriasManuais = dom.categoriasManuais();

    if (autoresManual) autoresManual.value = "";
    limparCategoriasManuais();

    if (autoresManuaisHidden) autoresManuaisHidden.innerHTML = "";
    if (categoriasManuaisHidden) categoriasManuaisHidden.innerHTML = "";

    limparPreviewCapa();

    if (modoCadastro) modoCadastro.value = "GOOGLE_BOOKS";

    if (modoAutomatico) modoAutomatico.classList.add("selecionado");
    if (modoManual) modoManual.classList.remove("selecionado");

    limparDadosLivro();

    if (buscaAutomatica) buscaAutomatica.style.display = "block";
    if (camposAnuncio) camposAnuncio.style.display = "none";

    if (capaAutomatica) capaAutomatica.style.display = "block";
    if (capaManual) capaManual.style.display = "none";

    if (titulo) titulo.readOnly = true;

    if (dataPublicacaoContainer) dataPublicacaoContainer.style.display = "block";

    if (autoresAutomaticos) autoresAutomaticos.style.display = "block";
    if (autoresManuais) autoresManuais.style.display = "none";

    if (categoriasAutomaticas) categoriasAutomaticas.style.display = "block";
    if (categoriasManuais) categoriasManuais.style.display = "none";
}

export function selecionarModoManual() {
    const modoCadastro = dom.modoCadastro();
    const modoManual = dom.modoManual();
    const modoAutomatico = dom.modoAutomatico();
    const buscaAutomatica = dom.buscaAutomatica();
    const camposAnuncio = dom.camposAnuncio();
    const capaAutomatica = dom.capaAutomatica();
    const capaManual = dom.capaManual();
    const titulo = dom.titulo();
    const dataPublicacaoContainer = dom.dataPublicacaoContainer();
    const autoresAutomaticos = dom.autoresAutomaticos();
    const autoresManuais = dom.autoresManuais();
    const categoriasAutomaticas = dom.categoriasAutomaticas();
    const categoriasManuais = dom.categoriasManuais();

    if (modoCadastro) modoCadastro.value = "MANUAL";

    if (modoManual) modoManual.classList.add("selecionado");
    if (modoAutomatico) modoAutomatico.classList.remove("selecionado");

    if (buscaAutomatica) buscaAutomatica.style.display = "none";

    limparDadosLivro();

    if (camposAnuncio) camposAnuncio.style.display = "block";

    if (capaAutomatica) capaAutomatica.style.display = "none";
    if (capaManual) capaManual.style.display = "block";

    if (titulo) titulo.readOnly = false;

    if (dataPublicacaoContainer) dataPublicacaoContainer.style.display = "none";

    if (autoresAutomaticos) autoresAutomaticos.style.display = "none";
    if (autoresManuais) autoresManuais.style.display = "block";

    if (categoriasAutomaticas) categoriasAutomaticas.style.display = "none";
    if (categoriasManuais) categoriasManuais.style.display = "block";
}

export function inicializarModo() {
    const modoAutoBtn = dom.modoAutomatico();
    const modoManualBtn = dom.modoManual();

    if (modoAutoBtn) {
        modoAutoBtn.addEventListener("click", selecionarModoAutomatico);
    }

    if (modoManualBtn) {
        modoManualBtn.addEventListener("click", selecionarModoManual);
    }
}
