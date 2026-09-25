package com.trocabook.Trocabook.controllers.response;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;

import java.util.List;

public record ChatConversaResponse(
        AnuncioDTO anuncio,
        UsuarioOutput usuarioNegociante,
        List<MensagemDTO> mensagens
) {
}