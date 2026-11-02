package com.trocabook.Trocabook.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NegociacaoAnalyticsDTO(
        String id,
        String anuncioId,
        String anuncioOferecidoId,
        List<String> categoriasAnuncio,
        List<String> categoriasAnuncioOferecido,
        LocalDateTime dataNegociacao,
        LocalDateTime dataFinalizacao,
        String tipoNegociacao,
        String status
) {
}
