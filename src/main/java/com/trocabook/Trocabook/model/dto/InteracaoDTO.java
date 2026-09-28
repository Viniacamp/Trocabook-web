package com.trocabook.Trocabook.model.dto;

import com.google.cloud.Timestamp;
import com.trocabook.Trocabook.model.Interacao;

public record InteracaoDTO(
        String id,
        String uidUsuario,
        Interacao.TipoInteracao tipoInteracao,
        String uidLivro,
        String uidAnuncio,
        String termoPesquisa,
        Interacao.OrigemInteracao origem,
        Timestamp dataInteracao
) {
}