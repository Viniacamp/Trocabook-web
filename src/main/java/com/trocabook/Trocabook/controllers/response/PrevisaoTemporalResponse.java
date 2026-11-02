package com.trocabook.Trocabook.controllers.response;

import java.util.List;
import java.util.Map;

public record PrevisaoTemporalResponse(
        Map<String, Object> avaliacao,
        List<Object> projecoes,
        List<Object> resumoHorizontes,
        Map<String, Object> modelo
) {
}