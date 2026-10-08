package com.trocabook.Trocabook.controllers.request;

public record CriarNegociacaoRequest(
        String anuncioId,
        String tipoNegociacao,
        String anuncioOferecidoId,
        String descricaoOferta
) {
}