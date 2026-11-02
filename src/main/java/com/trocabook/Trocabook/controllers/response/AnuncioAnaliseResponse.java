package com.trocabook.Trocabook.controllers.response;

import java.util.List;
import java.util.Map;

public record AnuncioAnaliseResponse(
        Map<String, Object> geral,
        List<Object> porTipo,
        List<Object> porAutor,
        List<Object> porCategoria,
        Map<String, Object> categoriaPorTipo,
        Map<String, Object> temporal,
        List<String> insights
) {
}
