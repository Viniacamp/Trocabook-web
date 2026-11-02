package com.trocabook.Trocabook.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnuncioAnalyticsDTO(
        String id,
        String uidUsuario,
        String uidLivro,
        String titulo,
        List<String> autores,
        List<String> categorias,
        String tipoNegociacao,
        LocalDateTime dataCriacao

) {
}
