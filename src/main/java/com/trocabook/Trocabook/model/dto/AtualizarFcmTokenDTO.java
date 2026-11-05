package com.trocabook.Trocabook.model.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarFcmTokenDTO(
        @NotBlank(message = "O token do FCM é obrigatório.")
        String fcmToken
) {}