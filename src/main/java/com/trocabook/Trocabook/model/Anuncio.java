package com.trocabook.Trocabook.model;

import com.google.cloud.Timestamp;
import com.trocabook.Trocabook.model.dto.AnuncioAnalyticsDTO;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Anuncio implements Serializable {

    private String id;

    private String uidUsuario;

    private String uidLivro;

    private String nomeUsuario;

    @NotNull(message = "Selecione o tipo de negociação")
    private TipoNegociacao tipoNegociacao;

    private String titulo;

    private String tituloBusca;

    private String fotoPerfil;

    private String capa;

    private List<String> autores;

    private List<String> categorias;

    private String descricao;

    private StatusAnuncio status;

    private Timestamp dataCriacao;

    public Anuncio() {
    }

    public Anuncio(String id, String uidUsuario, String uidLivro, String nomeUsuario, TipoNegociacao tipoNegociacao, String titulo, String fotoPerfil, String capa, List<String> autores, List<String> categorias, String descricao, StatusAnuncio status, Timestamp dataCriacao) {
        this.id = id;
        this.uidUsuario = uidUsuario;
        this.uidLivro = uidLivro;
        this.nomeUsuario = nomeUsuario;
        this.tipoNegociacao = tipoNegociacao;
        this.titulo = titulo;
        this.tituloBusca = normalizarTitulo(titulo);
        this.fotoPerfil = fotoPerfil;
        this.capa = capa;
        this.autores = autores;
        this.categorias = categorias;
        this.descricao = descricao;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUidUsuario() {
        return uidUsuario;
    }

    public void setUidUsuario(String uidUsuario) {
        this.uidUsuario = uidUsuario;
    }

    public String getUidLivro() {
        return uidLivro;
    }

    public void setUidLivro(String uidLivro) {
        this.uidLivro = uidLivro;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public TipoNegociacao getTipoNegociacao() {
        return tipoNegociacao;
    }

    public void setTipoNegociacao(TipoNegociacao tipoNegociacao) {
        this.tipoNegociacao = tipoNegociacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTituloBusca() {
        return tituloBusca;
    }

    public void setTituloBusca(String tituloBusca) {
        this.tituloBusca = tituloBusca;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    public List<String> getAutores() {
        return autores;
    }

    public void setAutores(List<String> autores) {
        this.autores = autores;
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<String> categorias) {
        this.categorias = categorias;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusAnuncio getStatus() {
        return status;
    }

    public void setStatus(StatusAnuncio status) {
        this.status = status;
    }

    public Timestamp getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Timestamp dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public enum TipoNegociacao {
        TROCA, VENDA, AMBOS
    }

    public enum StatusAnuncio {
        ATIVO,
        FINALIZADO
    }


    public AnuncioDTO paraDto(List<String> autores, List<String> categorias){
        return new AnuncioDTO(
                this.id,
                this.uidUsuario,
                this.uidLivro,
                this.nomeUsuario,
                this.tipoNegociacao.name(),
                this.titulo,
                this.fotoPerfil,
                this.capa,
                autores,
                categorias,
                this.descricao,
                this.status != null
                        ? this.status.name()
                        : StatusAnuncio.ATIVO.name()
        );
    }

    public AnuncioAnalyticsDTO paraAnalytics(List<String> autores, List<String> categorias){
        return new AnuncioAnalyticsDTO(
                this.id,
                this.uidUsuario,
                this.uidLivro,
                this.tituloBusca,
                autores,
                categorias,
                tipoNegociacao.name(),
                this.dataCriacao != null ? LocalDateTime.ofInstant(this.dataCriacao.toDate().toInstant(), ZoneId.of("America/Sao_Paulo")) : null
        );
    }

    private static String normalizarTitulo(String titulo) {
        if (titulo == null) {
            return "";
        }

        return titulo.toLowerCase(Locale.ROOT);
    }

}
