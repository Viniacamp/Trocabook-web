package com.trocabook.Trocabook.model.dto;

public record UsuarioStatusNotificacaoDTO(
        String uidUsuario,
        String email,
        String fcmToken
) {}