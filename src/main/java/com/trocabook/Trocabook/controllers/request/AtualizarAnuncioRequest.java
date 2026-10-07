package com.trocabook.Trocabook.controllers.request;

public record AtualizarAnuncioRequest(
        String descricao,
        String tipoNegociacao
) {
}