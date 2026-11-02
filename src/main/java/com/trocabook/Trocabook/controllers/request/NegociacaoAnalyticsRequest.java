package com.trocabook.Trocabook.controllers.request;

import com.trocabook.Trocabook.model.dto.NegociacaoAnalyticsDTO;
import java.util.List;

public record NegociacaoAnalyticsRequest(
        List<NegociacaoAnalyticsDTO> negociacoes
) {
}