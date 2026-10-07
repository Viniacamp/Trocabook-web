package com.trocabook.Trocabook.controllers.response;

import com.trocabook.Trocabook.model.dto.MensagemDTO;
import com.trocabook.Trocabook.model.dto.NegociacaoDTO;

import java.util.List;

public record ChatAtualizacaoResponse(
        List<MensagemDTO> mensagens,
        NegociacaoDTO negociacao
) {}
