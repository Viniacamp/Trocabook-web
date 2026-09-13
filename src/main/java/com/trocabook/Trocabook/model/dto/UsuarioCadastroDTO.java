package com.trocabook.Trocabook.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

// Este DTO (Data Transfer Object) carrega os dados do formulário de cadastro
// e contém as validações que serão aplicadas ANTES da criptografia da senha.
public class UsuarioCadastroDTO {

    @NotBlank(message = "Preencha o Nome")
    @Pattern(regexp = "^[A-Za-záàâãéèêíïóôõöúçÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇ ]+$", message = "O nome deve conter apenas letras e espaços.")
    private String nmUsuario;

    @CPF(message = "CPF inválido")
    private String CPF;

    @NotBlank(message = "Preencha o E-mail")
    @Email(message = "Preencha com um E-mail válido")
    private String email;

    // A validação da senha forte fica AQUI, no DTO!
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "A senha deve conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial (@$!%*?&).")
    private String senha;

    // --- Getters e Setters ---

    public String getNmUsuario() {
        return nmUsuario;
    }

    public void setNmUsuario(String nmUsuario) {
        this.nmUsuario = nmUsuario;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}