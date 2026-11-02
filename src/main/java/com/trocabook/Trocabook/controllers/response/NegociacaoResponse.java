package com.trocabook.Trocabook.controllers.response;

import java.util.List;
import java.util.Map;

public record NegociacaoResponse(
        Map<String, Object> geral,
        List<Object> porTipo,
        List<Object> porCategoria,
        List<Object> afinidadeCategorias,
        List<Object> combinacoesCategorias,
        Map<String, Object> temporal,
        List<Object> insights
) {
}
