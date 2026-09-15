package com.trocabook.Trocabook.model;

import com.trocabook.Trocabook.model.dto.UsuarioInput;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.io.Serializable;

public class Usuario implements Serializable {

    private String id;

    @NotBlank(message = "Preencha o Nome")
    @Pattern(
            regexp = "^[A-Za-záàâãéèêíïóôõöúçÁÀÂÃÉÈÊÍÏÓÔÕÖÚÇ ]+$",
            message = "O nome deve conter apenas letras e espaços."
    )
    private String nome;

    @CPF(message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "Preencha o E-mail")
    @Email(message = "Preencha com um E-mail válido")
    private String emailPrincipal;

    @Email(message = "E-mail de recuperação inválido")
    private String emailRecuperacao;

    private String fotoPerfil;

    private String genero;


    private String nascimento;

    @NotBlank(message = "Preencha o RG")
    private String rg;

    @NotBlank(message = "Preencha o telefone")
    private String telefone;

    private String status;

    @Max(5)
    private double avaliacao;

    public Usuario() {
    }

    public Usuario(String id, String nome, String cpf,
                   String emailPrincipal, String emailRecuperacao,
                   String fotoPerfil, String genero, String nascimento,
                   String rg, String telefone, String status,
                   double avaliacao) {

        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.emailPrincipal = emailPrincipal;
        this.emailRecuperacao = emailRecuperacao;
        this.fotoPerfil = fotoPerfil;
        this.genero = genero;
        this.nascimento = nascimento;
        this.rg = rg;
        this.telefone = telefone;
        this.status = status;
        this.avaliacao = avaliacao;
    }

    public Usuario(String nome, String cpf, String emailPrincipal, String emailRecuperacao, String fotoPerfil, String genero, String nascimento, String rg, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.emailPrincipal = emailPrincipal;
        this.emailRecuperacao = emailRecuperacao;
        this.fotoPerfil = fotoPerfil;
        this.genero = genero;
        this.nascimento = nascimento;
        this.rg = rg;
        this.telefone = telefone;
    }

    public Usuario(Usuario outroUsuario) {

        if (outroUsuario == null) {
            return;
        }

        this.id = outroUsuario.id;
        this.nome = outroUsuario.nome;
        this.cpf = outroUsuario.cpf;
        this.emailPrincipal = outroUsuario.emailPrincipal;
        this.emailRecuperacao = outroUsuario.emailRecuperacao;
        this.fotoPerfil = outroUsuario.fotoPerfil;
        this.genero = outroUsuario.genero;
        this.nascimento = outroUsuario.nascimento;
        this.rg = outroUsuario.rg;
        this.telefone = outroUsuario.telefone;
        this.status = outroUsuario.status;
        this.avaliacao = outroUsuario.avaliacao;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmailPrincipal() {
        return emailPrincipal;
    }

    public void setEmailPrincipal(String emailPrincipal) {
        this.emailPrincipal = emailPrincipal;
    }

    public String getEmailRecuperacao() {
        return emailRecuperacao;
    }

    public void setEmailRecuperacao(String emailRecuperacao) {
        this.emailRecuperacao = emailRecuperacao;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getNascimento() {
        return nascimento;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(double avaliacao) {
        this.avaliacao = avaliacao;
    }


    public static Usuario from(UsuarioInput input){
        return new Usuario(
                input.nome(),
                input.cpf(),
                input.emailPrincipal(),
                input.emailRecuperacao(),
                input.fotoPerfil(),
                input.genero(),
                input.nascimento() != null ? input.nascimento().toString() : null,
                input.rg(),
                input.telefone()
        );
    }

    public UsuarioOutput paraOutput(){
        return new UsuarioOutput(
                this.getId(),
                this.getNome(),
                this.getFotoPerfil()
        );
    }



}