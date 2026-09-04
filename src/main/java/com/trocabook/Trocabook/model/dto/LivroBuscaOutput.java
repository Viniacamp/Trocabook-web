package com.trocabook.Trocabook.model.dto;

import java.util.List;

public record LivroBuscaOutput(
        String googleBooksId,
        String titulo,
        List<String> autores,
        String publicadora,
        String dataPublicacao,
        String urlImagem,
        String lingua,
        List<String> categorias
) {
}