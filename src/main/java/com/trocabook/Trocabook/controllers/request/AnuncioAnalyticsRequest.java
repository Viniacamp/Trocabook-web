package com.trocabook.Trocabook.controllers.request;

import com.trocabook.Trocabook.model.dto.AnuncioAnalyticsDTO;
import java.util.List;

public record AnuncioAnalyticsRequest(
        List<AnuncioAnalyticsDTO> anuncios,
        Integer horizontePrevisao,
        Integer quantidadeTeste
) {
    public AnuncioAnalyticsRequest(List<AnuncioAnalyticsDTO> anuncios) {
        this(anuncios, 12, 6);
    }
}