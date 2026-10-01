package com.trocabook.Trocabook.controllers.request;

import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.InteracaoDTO;

import java.util.List;

public record RecomendacaoRequest(
        String uidUsuario,
        List<AnuncioDTO> anuncios,
        List<InteracaoDTO> interacoes
) {
    public RecomendacaoRequest() {
        this(null, null, null);
    }
}