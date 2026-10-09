/**
 * Módulo responsável pelo gerenciamento de categorias (modal, seleção manual e complementação automática).
 */

import { dom } from './ui.js';
import {
    getCategoriasConfirmadas,
    setCategoriasConfirmadas,
    removerCategoriaConfirmada,
    getCategoriasAutomaticas,
    setCategoriasAutomaticas,
    removerCategoriaAutomatica,
    getModoSelecaoCategorias,
    setModoSelecaoCategorias
} from './state.js';

export function encontrarCategoria(nome) {
    return [...document.querySelectorAll(".categoria-checkbox")].find(
        checkbox =>
            checkbox.value.localeCompare(nome, undefined, {
                sensitivity: "base"
            }) === 0
    );
}

export function restaurarCheckboxesConfirmados() {
    const confirmadas = getCategoriasConfirmadas();
    document
        .querySelectorAll(".categoria-checkbox")
        .forEach(checkbox => {
            checkbox.checked = confirmadas.includes(checkbox.value);
        });
}

export function adicionarNovaCategoriaAoModal(nome) {
    const listaCategorias = dom.listaCategorias();
    if (!listaCategorias) return;

    const label = document.createElement("label");
    label.className = "categoria-opcao";
    label.dataset.novaCategoria = "true";

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.className = "categoria-checkbox";
    checkbox.value = nome;
    checkbox.checked = true;

    const span = document.createElement("span");
    span.textContent = nome;

    label.appendChild(checkbox);
    label.appendChild(span);

    const outraCategoria = listaCategorias.querySelector(".outra-categoria");
    listaCategorias.insertBefore(label, outraCategoria);
}

export function criarInputsCategoriasSelecionadas() {
    const container = dom.categoriasManuaisHidden();
    if (!container) return;

    container.innerHTML = "";

    getCategoriasConfirmadas().forEach((categoria, indice) => {
        const hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = `categorias[${indice}]`;
        hiddenInput.value = categoria;

        container.appendChild(hiddenInput);
    });
}

export function atualizarCategoriasSelecionadas() {
    const categoriasSelecionadas = dom.categoriasSelecionadas();
    if (!categoriasSelecionadas) return;

    categoriasSelecionadas.innerHTML = "";

    getCategoriasConfirmadas().forEach(nome => {
        const tag = document.createElement("span");
        tag.className = "categoria-tag";

        const texto = document.createElement("span");
        texto.textContent = nome;

        const remover = document.createElement("button");
        remover.type = "button";
        remover.innerHTML = "&times;";
        remover.setAttribute("aria-label", `Remover ${nome}`);

        remover.addEventListener("click", function () {
            removerCategoriaConfirmada(nome);

            const checkbox = encontrarCategoria(nome);
            if (checkbox) {
                checkbox.checked = false;
            }

            atualizarCategoriasSelecionadas();
        });

        tag.appendChild(texto);
        tag.appendChild(remover);

        categoriasSelecionadas.appendChild(tag);
    });

    criarInputsCategoriasSelecionadas();
}

export function atualizarCategoriasAutomaticas() {
    const categoriasDiv = dom.categoriasDiv();
    if (!categoriasDiv) return;

    categoriasDiv.innerHTML = "";

    getCategoriasAutomaticas().forEach((categoria, indice) => {
        const tag = document.createElement("span");
        tag.className = "tag-editavel";

        const texto = document.createElement("span");
        texto.textContent = categoria;

        const remover = document.createElement("button");
        remover.type = "button";
        remover.innerHTML = "&times;";
        remover.setAttribute("aria-label", `Remover ${categoria}`);

        remover.addEventListener("click", function () {
            removerCategoriaAutomatica(categoria);
            atualizarCategoriasAutomaticas();
        });

        const hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = `categorias[${indice}]`;
        hiddenInput.value = categoria;

        tag.appendChild(texto);
        tag.appendChild(remover);

        categoriasDiv.appendChild(tag);
        categoriasDiv.appendChild(hiddenInput);
    });
}

export function fecharModalCategorias() {
    const modo = getModoSelecaoCategorias();
    const categoriasAtuais =
        modo === "AUTOMATICO"
            ? getCategoriasAutomaticas()
            : getCategoriasConfirmadas();

    document
        .querySelectorAll('.categoria-opcao[data-nova-categoria="true"]')
        .forEach(label => {
            const checkbox = label.querySelector(".categoria-checkbox");
            if (!categoriasAtuais.includes(checkbox.value)) {
                label.remove();
            }
        });

    if (modo === "AUTOMATICO") {
        const catAuto = getCategoriasAutomaticas();
        document
            .querySelectorAll(".categoria-checkbox")
            .forEach(checkbox => {
                checkbox.checked = catAuto.some(
                    categoria =>
                        categoria.localeCompare(checkbox.value, undefined, {
                            sensitivity: "base"
                        }) === 0
                );
            });
    } else {
        restaurarCheckboxesConfirmados();
    }

    const checkboxNova = dom.checkboxNovaCategoria();
    if (checkboxNova) checkboxNova.checked = false;

    const modal = dom.modalCategorias();
    if (modal) modal.style.display = "none";
}

export function limparCategoriasManuais() {
    setCategoriasConfirmadas([]);

    document
        .querySelectorAll(".categoria-checkbox")
        .forEach(checkbox => {
            checkbox.checked = false;
        });

    document
        .querySelectorAll('.categoria-opcao[data-nova-categoria="true"]')
        .forEach(label => label.remove());

    const checkboxNova = dom.checkboxNovaCategoria();
    if (checkboxNova) checkboxNova.checked = false;

    const selecionadas = dom.categoriasSelecionadas();
    if (selecionadas) selecionadas.innerHTML = "";

    const hidden = dom.categoriasManuaisHidden();
    if (hidden) hidden.innerHTML = "";

    const nova = dom.novaCategoria();
    if (nova) nova.value = "";

    const modalCat = dom.modalCategorias();
    if (modalCat) modalCat.style.display = "none";

    const modalNova = dom.modalNovaCategoria();
    if (modalNova) modalNova.style.display = "none";
}

export function inicializarCategorias() {
    const btnSelecionar = dom.btnSelecionarCategorias();
    const btnCancelar = dom.btnCancelarCategorias();
    const btnFechar = dom.btnFecharCategorias();
    const checkboxNova = dom.checkboxNovaCategoria();
    const btnCancelarNova = dom.btnCancelarNovaCategoria();
    const btnAdicionarNova = dom.btnAdicionarNovaCategoria();
    const btnConfirmar = dom.btnConfirmarCategorias();
    const btnComplementar = dom.btnComplementarCategorias();

    if (btnSelecionar) {
        btnSelecionar.addEventListener("click", function () {
            setModoSelecaoCategorias("MANUAL");
            restaurarCheckboxesConfirmados();
            const modal = dom.modalCategorias();
            if (modal) modal.style.display = "flex";
        });
    }

    if (btnCancelar) {
        btnCancelar.addEventListener("click", fecharModalCategorias);
    }

    if (btnFechar) {
        btnFechar.addEventListener("click", fecharModalCategorias);
    }

    if (checkboxNova) {
        checkboxNova.addEventListener("change", function () {
            if (!this.checked) return;

            const inputNova = dom.novaCategoria();
            if (inputNova) inputNova.value = "";

            const modalNova = dom.modalNovaCategoria();
            if (modalNova) {
                modalNova.style.display = "flex";
                if (inputNova) inputNova.focus();
            }
        });
    }

    if (btnCancelarNova) {
        btnCancelarNova.addEventListener("click", function () {
            const inputNova = dom.novaCategoria();
            if (inputNova) inputNova.value = "";

            if (checkboxNova) checkboxNova.checked = false;

            const modalNova = dom.modalNovaCategoria();
            if (modalNova) modalNova.style.display = "none";
        });
    }

    if (btnAdicionarNova) {
        btnAdicionarNova.addEventListener("click", function () {
            const inputNova = dom.novaCategoria();
            const nome = inputNova ? inputNova.value.trim() : "";

            if (nome.length === 0) {
                window.alert("Informe o nome da categoria.");
                return;
            }

            const checkboxExistente = encontrarCategoria(nome);

            if (checkboxExistente) {
                checkboxExistente.checked = true;
            } else {
                adicionarNovaCategoriaAoModal(nome);
            }

            if (inputNova) inputNova.value = "";
            if (checkboxNova) checkboxNova.checked = false;

            const modalNova = dom.modalNovaCategoria();
            if (modalNova) modalNova.style.display = "none";
        });
    }

    if (btnConfirmar) {
        btnConfirmar.addEventListener("click", function () {
            const selecionadas = [
                ...document.querySelectorAll(".categoria-checkbox:checked")
            ].map(checkbox => checkbox.value);

            if (getModoSelecaoCategorias() === "AUTOMATICO") {
                const catAuto = getCategoriasAutomaticas();
                const categoriasApiNaoPresentesNoModal = catAuto.filter(
                    categoria => !encontrarCategoria(categoria)
                );

                setCategoriasAutomaticas([
                    ...new Set([
                        ...categoriasApiNaoPresentesNoModal,
                        ...selecionadas
                    ])
                ]);

                atualizarCategoriasAutomaticas();
            } else {
                setCategoriasConfirmadas(selecionadas);
                atualizarCategoriasSelecionadas();
            }

            const modal = dom.modalCategorias();
            if (modal) modal.style.display = "none";
        });
    }

    if (btnComplementar) {
        btnComplementar.addEventListener("click", function () {
            setModoSelecaoCategorias("AUTOMATICO");

            const catAuto = getCategoriasAutomaticas();
            document
                .querySelectorAll(".categoria-checkbox")
                .forEach(checkbox => {
                    checkbox.checked = catAuto.some(
                        categoria =>
                            categoria.localeCompare(
                                checkbox.value,
                                undefined,
                                { sensitivity: "base" }
                            ) === 0
                    );
                });

            const modal = dom.modalCategorias();
            if (modal) modal.style.display = "flex";
        });
    }
}
