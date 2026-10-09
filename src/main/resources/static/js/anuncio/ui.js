/**
 * Referências e utilitários de interface para a página de anúncio.
 */

export const dom = {
    // Busca
    inputBusca: () => document.getElementById("tituloBuscado"),
    resultadosBusca: () => document.getElementById("resultadosLivros"),
    containerBusca: () => document.querySelector(".busca-livro-container"),
    botaoBuscar: () => document.getElementById("buscarLivro"),

    // Modos
    modoAutomatico: () => document.getElementById("modoAutomatico"),
    modoManual: () => document.getElementById("modoManual"),
    modoCadastro: () => document.getElementById("modoCadastro"),
    buscaAutomatica: () => document.getElementById("buscaAutomatica"),
    camposAnuncio: () => document.getElementById("camposAnuncio"),

    // Campos do Livro
    titulo: () => document.getElementById("titulo"),
    dataPublicacaoContainer: () => document.getElementById("dataPublicacaoContainer"),
    dataPublicacao: () => document.getElementById("dataPublicacao"),
    lingua: () => document.getElementById("lingua"),
    googleBooksId: () => document.getElementById("googleBooksId"),
    urlImagem: () => document.getElementById("urlImagem"),
    descricao: () => document.getElementById("descricao"),
    tipoNegociacao: () => document.getElementById("tipoNegociacao"),
    formAnuncio: () => document.getElementById("formAnuncio"),

    // Capa / Imagem
    capaAutomatica: () => document.getElementById("capaAutomatica"),
    capaLivro: () => document.getElementById("capaLivro"),
    capaManual: () => document.getElementById("capaManual"),
    inputImagem: () => document.getElementById("imagem"),
    uploadPromptLivro: () => document.getElementById("upload-prompt-livro"),
    previewContainerLivro: () => document.getElementById("preview-container-livro"),
    imagePreviewLivro: () => document.getElementById("image-preview-livro"),
    removeImageBtn: () => document.getElementById("remove-image-btn"),
    uploadLabelLivro: () => document.querySelector("#capaManual .file-upload-label"),

    // Autores
    autoresAutomaticos: () => document.getElementById("autoresAutomaticos"),
    autoresDiv: () => document.getElementById("autores"),
    novoAutorAutomatico: () => document.getElementById("novoAutorAutomatico"),
    btnAdicionarAutorAutomatico: () => document.getElementById("btnAdicionarAutorAutomatico"),
    autoresManuais: () => document.getElementById("autoresManuais"),
    autoresManual: () => document.getElementById("autoresManual"),
    autoresManuaisHidden: () => document.getElementById("autoresManuaisHidden"),

    // Categorias
    categoriasAutomaticas: () => document.getElementById("categoriasAutomaticas"),
    categoriasDiv: () => document.getElementById("categorias"),
    btnComplementarCategorias: () => document.getElementById("btnComplementarCategorias"),
    categoriasManuais: () => document.getElementById("categoriasManuais"),
    categoriasSelecionadas: () => document.getElementById("categoriasSelecionadas"),
    btnSelecionarCategorias: () => document.getElementById("btnSelecionarCategorias"),
    categoriasManuaisHidden: () => document.getElementById("categoriasManuaisHidden"),

    // Modal Categorias
    modalCategorias: () => document.getElementById("modalCategorias"),
    btnFecharCategorias: () => document.getElementById("btnFecharCategorias"),
    btnCancelarCategorias: () => document.getElementById("btnCancelarCategorias"),
    btnConfirmarCategorias: () => document.getElementById("btnConfirmarCategorias"),
    listaCategorias: () => document.getElementById("listaCategorias"),
    checkboxNovaCategoria: () => document.getElementById("checkboxNovaCategoria"),

    // Modal Nova Categoria
    modalNovaCategoria: () => document.getElementById("modalNovaCategoria"),
    novaCategoria: () => document.getElementById("novaCategoria"),
    btnCancelarNovaCategoria: () => document.getElementById("btnCancelarNovaCategoria"),
    btnAdicionarNovaCategoria: () => document.getElementById("btnAdicionarNovaCategoria")
};

/**
 * Obtém o CSRF token presente no formulário.
 */
export function getCsrfToken() {
    const csrfInput = document.querySelector('input[name="_csrf"]');
    return csrfInput ? csrfInput.value : "";
}
