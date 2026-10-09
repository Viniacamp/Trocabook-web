/**
 * Módulo responsável pela gestão de upload e preview da capa do livro (fluxo manual).
 */

import { dom } from './ui.js';

export function arquivoImagemValido(arquivo) {
    if (!arquivo) return false;

    const tiposPermitidos = [
        "image/jpeg",
        "image/png",
        "image/gif"
    ];

    return tiposPermitidos.includes(arquivo.type);
}

export function exibirPreviewCapa(arquivo) {
    const inputImagem = dom.inputImagem();
    const imagePreviewLivro = dom.imagePreviewLivro();
    const uploadPromptLivro = dom.uploadPromptLivro();
    const previewContainerLivro = dom.previewContainerLivro();

    if (!arquivoImagemValido(arquivo)) {
        window.alert("Selecione um arquivo de imagem.");
        if (inputImagem) inputImagem.value = "";
        return;
    }

    const reader = new FileReader();

    reader.onload = function (event) {
        if (imagePreviewLivro) {
            imagePreviewLivro.src = event.target.result;
        }
        if (uploadPromptLivro) {
            uploadPromptLivro.style.display = "none";
        }
        if (previewContainerLivro) {
            previewContainerLivro.style.display = "block";
        }
    };

    reader.readAsDataURL(arquivo);
}

export function limparPreviewCapa() {
    const inputImagem = dom.inputImagem();
    const imagePreviewLivro = dom.imagePreviewLivro();
    const uploadPromptLivro = dom.uploadPromptLivro();
    const previewContainerLivro = dom.previewContainerLivro();

    if (inputImagem) inputImagem.value = "";
    if (imagePreviewLivro) imagePreviewLivro.src = "#";
    if (previewContainerLivro) previewContainerLivro.style.display = "none";
    if (uploadPromptLivro) uploadPromptLivro.style.display = "block";
}

export function inicializarImagem() {
    const inputImagem = dom.inputImagem();
    const removeImageBtn = dom.removeImageBtn();
    const uploadLabelLivro = dom.uploadLabelLivro();

    if (inputImagem) {
        inputImagem.addEventListener("change", function () {
            const arquivo = this.files[0];
            if (arquivo) {
                exibirPreviewCapa(arquivo);
            }
        });
    }

    if (removeImageBtn) {
        removeImageBtn.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();
            limparPreviewCapa();
        });
    }

    if (uploadLabelLivro) {
        ["dragenter", "dragover"].forEach(evento => {
            uploadLabelLivro.addEventListener(evento, function (event) {
                event.preventDefault();
                event.stopPropagation();
                uploadLabelLivro.classList.add("is-dragover");
            });
        });

        ["dragleave", "drop"].forEach(evento => {
            uploadLabelLivro.addEventListener(evento, function (event) {
                event.preventDefault();
                event.stopPropagation();
                uploadLabelLivro.classList.remove("is-dragover");
            });
        });

        uploadLabelLivro.addEventListener("drop", function (event) {
            const arquivos = event.dataTransfer.files;

            if (!arquivos || arquivos.length === 0) {
                return;
            }

            const arquivo = arquivos[0];

            if (!arquivoImagemValido(arquivo)) {
                window.alert("Selecione um arquivo de imagem.");
                return;
            }

            const dataTransfer = new DataTransfer();
            dataTransfer.items.add(arquivo);

            if (inputImagem) {
                inputImagem.files = dataTransfer.files;
            }

            exibirPreviewCapa(arquivo);
        });
    }
}
