package com.trocabook.Trocabook.controllers.response;

public record RecomendacaoResponse(
        String uidAnuncio,
        double score
) {
    public RecomendacaoResponse() {
        this(null, 0.0);
    }
}