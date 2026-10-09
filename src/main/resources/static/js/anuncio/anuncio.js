/**
 * Entry point do JavaScript da página de anúncio (anunciar.html).
 * Orquestra e inicializa os submódulos da página.
 */

import { inicializarModo } from './modo.js';
import { inicializarBusca } from './busca.js';
import { inicializarAutores } from './autores.js';
import { inicializarCategorias } from './categorias.js';
import { inicializarImagem } from './imagem.js';
import { inicializarValidacao } from './validacao.js';

export function inicializarAnuncio() {
    inicializarModo();
    inicializarBusca();
    inicializarAutores();
    inicializarCategorias();
    inicializarImagem();
    inicializarValidacao();
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", inicializarAnuncio);
} else {
    inicializarAnuncio();
}
