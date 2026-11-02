package com.trocabook.Trocabook.controllers.response;

public record AnuncioResponse(
        AnuncioAnaliseResponse analise,
        PrevisaoTemporalResponse previsao
) {
}
