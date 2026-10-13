package com.trocabook.Trocabook.model.dto;

import com.trocabook.Trocabook.model.Usuario;

public record AdminUsuarioOutput(
        String id,
        String nome,
        String emailPrincipal,
        String fotoPerfil,
        String status,
        double avaliacao,
        Usuario.Role role
) {
}