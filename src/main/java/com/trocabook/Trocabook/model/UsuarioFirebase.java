package com.trocabook.Trocabook.model;

import com.trocabook.Trocabook.model.dto.UsuarioFirebaseInput;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseOutput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UsuarioFirebase implements Serializable {

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

    @NotBlank(message = "Preencha a data de nascimento")
    private LocalDate nascimento;

    @NotBlank(message = "Preencha o RG")
    private String rg;

    @NotBlank(message = "Preencha o telefone")
    private String telefone;

    private String status;

    @Max(5)
    private double avaliacao;

    private String resetPasswordToken;

    private LocalDateTime resetPasswordTokenExpiryDate;

    public UsuarioFirebase() {
    }

    public UsuarioFirebase(String id, String nome, String cpf,
                   String emailPrincipal, String emailRecuperacao,
                   String fotoPerfil, String genero, LocalDate nascimento,
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

    public UsuarioFirebase(String nome, String cpf, String emailPrincipal, String emailRecuperacao, String fotoPerfil, String genero, LocalDate nascimento, String rg, String telefone) {
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

    public UsuarioFirebase(UsuarioFirebase outroUsuario) {

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
        this.resetPasswordToken = outroUsuario.resetPasswordToken;
        this.resetPasswordTokenExpiryDate =
                outroUsuario.resetPasswordTokenExpiryDate;
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

    public LocalDate getNascimento() {
        return nascimento;
    }

    public void setNascimento(LocalDate nascimento) {
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

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public LocalDateTime getResetPasswordTokenExpiryDate() {
        return resetPasswordTokenExpiryDate;
    }

    public void setResetPasswordTokenExpiryDate(
            LocalDateTime resetPasswordTokenExpiryDate) {
        this.resetPasswordTokenExpiryDate =
                resetPasswordTokenExpiryDate;
    }

    public static UsuarioFirebase from(UsuarioFirebaseInput input){
        return new UsuarioFirebase(
                input.nome(),
                input.cpf(),
                input.emailPrincipal(),
                input.emailRecuperacao(),
                input.fotoPerfil(),
                input.genero(),
                input.nascimento(),
                input.rg(),
                input.telefone()
        );
    }

    public UsuarioFirebaseOutput paraOutput(){
        return new UsuarioFirebaseOutput(
                this.getId(),
                this.getNome(),
                this.getFotoPerfil()
        );
    }



}