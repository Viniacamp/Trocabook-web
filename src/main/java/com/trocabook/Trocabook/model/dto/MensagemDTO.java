package com.trocabook.Trocabook.model.dto;

import java.time.LocalDateTime;

public record MensagemDTO(
        String id,
        String uidRemetente,
        String uidDestinatario,
        String uidAnuncio,
        String conteudo,
        LocalDateTime dataEnvio
){
    public MensagemDTO(String uidRemetente, String uidDestinatario, String uidAnuncio){
        this(null, uidRemetente, uidDestinatario, uidAnuncio, null, null);
    }
}

