package com.trocabook.Trocabook.model.dto;

import java.util.List;

public record AnuncioDTO(
        String id,
        String uidUsuario,

        String uidLivro,

        String nomeUsuario,

        String tipoNegociacao,

        String titulo,

        String fotoPerfil,

        String capa,

        List<String> autores,

        List<String> categorias) {
}
