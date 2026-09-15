package com.trocabook.Trocabook.controllers.request;

import java.util.List;

public record AnunciarLivroRequest(
        String googleBooksId,
        String titulo,
        List<String> autores,
        String publicadora,
        String dataPublicacao,
        String urlImagem,
        String lingua,
        List<String> categorias,
        String tipoNegociacao
) {
    public AnunciarLivroRequest() {
        this(null, null, null, null, null, null, null, null, null);
    }
}