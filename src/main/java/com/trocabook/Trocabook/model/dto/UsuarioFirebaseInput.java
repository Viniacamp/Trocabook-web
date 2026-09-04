package com.trocabook.Trocabook.model.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record UsuarioFirebaseInput(
        @NotBlank(message = "Preencha o Nome")
        String nome,

        @CPF(message = "CPF inválido")
        String cpf,

        @NotBlank(message = "Preencha o E-mail")
        @Email(message = "Preencha com um E-mail válido")
        String emailPrincipal,

        @Email(message = "E-mail de recuperação inválido")
        String emailRecuperacao,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String senha,

        String fotoPerfil,

        String genero,

        @NotNull(message = "Preencha a data de nascimento")
        LocalDate nascimento,

        @NotBlank(message = "Preencha o RG")
        String rg,

        @NotBlank(message = "Preencha o telefone")
        String telefone) {


     public UsuarioFirebaseInput(
            String nome,
            String cpf,
            String emailPrincipal,
            String emailRecuperacao,
            String fotoPerfil,
            String genero,
            LocalDate nascimento,
            String rg,
            String telefone) {

         this(
                 nome,
                 cpf,
                 emailPrincipal,
                 emailRecuperacao,
                 null,
                 fotoPerfil,
                 genero,
                 nascimento,
                 rg,
                 telefone
         );
    }

}
