package com.trocabook.Trocabook.model.dto;




public record NegociacaoDTO(
        String id,
        String usuarioAnuncianteId,
        String usuarioCompradorId,
        String anuncioId,
        String dataNegociacao,
        String dataFinalizacao,
        String tipoNegociacao,
        String status,
        String nmAnunciante,
        String fotoPerfilAnunciante,
        String nmComprador,
        String fotoPerfilComprador,
        String titulo,
        String capa,
        boolean confirmacaoAnunciante,
        boolean confirmacaoComprador,
        String anuncioOferecidoId,
        String tituloLivroOferecido,
        String capaLivroOferecido,
        String descricaoOferta
) {
}