/**
 * Módulo responsável pela gestão de autores (fluxos automático e manual).
 */

import { dom } from './ui.js';
import {
    getAutoresAutomaticos,
    adicionarAutorAutomatico,
    removerAutorAutomatico
} from './state.js';

export function atualizarAutoresAutomaticos() {
    const autoresDiv = dom.autoresDiv();
    if (!autoresDiv) return;

    autoresDiv.innerHTML = "";

    const autores = getAutoresAutomaticos();

    autores.forEach((autor, indice) => {
        const tag = document.createElement("span");
        tag.className = "tag-editavel";

        const texto = document.createElement("span");
        texto.textContent = autor;

        const remover = document.createElement("button");
        remover.type = "button";
        remover.innerHTML = "&times;";
        remover.setAttribute("aria-label", `Remover ${autor}`);

        remover.addEventListener("click", function () {
            removerAutorAutomatico(autor);
            atualizarAutoresAutomaticos();
        });

        const hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = `autores[${indice}]`;
        hiddenInput.value = autor;

        tag.appendChild(texto);
        tag.appendChild(remover);

        autoresDiv.appendChild(tag);
        autoresDiv.appendChild(hiddenInput);
    });
}

export function criarInputsManuais(inputId, containerId, nomeCampo) {
    const input = document.getElementById(inputId);
    const container = document.getElementById(containerId);

    if (!input || !container) return;

    container.innerHTML = "";

    const valores = input.value
        .split(",")
        .map(valor => valor.trim())
        .filter(valor => valor.length > 0);

    valores.forEach((valor, indice) => {
        const hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = `${nomeCampo}[${indice}]`;
        hiddenInput.value = valor;

        container.appendChild(hiddenInput);
    });
}

export function inicializarAutores() {
    const btnAdicionar = dom.btnAdicionarAutorAutomatico();

    if (btnAdicionar) {
        btnAdicionar.addEventListener("click", function () {
            const input = dom.novoAutorAutomatico();
            if (!input) return;

            const autor = input.value.trim();

            if (!autor) {
                window.alert("Informe o nome do autor.");
                return;
            }

            const jaExiste = getAutoresAutomaticos().some(
                existente =>
                    existente.localeCompare(autor, undefined, {
                        sensitivity: "base"
                    }) === 0
            );

            if (jaExiste) {
                window.alert("Este autor já foi adicionado.");
                return;
            }

            adicionarAutorAutomatico(autor);
            input.value = "";
            atualizarAutoresAutomaticos();
        });
    }
}
