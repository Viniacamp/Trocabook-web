package com.trocabook.Trocabook.model.dto;


import java.time.LocalDateTime;

public record NegociacaoDTO(
        String id,
        String usuarioAnuncianteId,
        String usuarioCompradorId,
        String anuncioId,
        LocalDateTime dataNegociacao,
        String tipoNegociacao,
        String nmAnunciante,
        String fotoPerfilAnunciante,
        String nmComprador,
        String fotoPerfilComprador,
        String titulo,
        String capa
) {
}
