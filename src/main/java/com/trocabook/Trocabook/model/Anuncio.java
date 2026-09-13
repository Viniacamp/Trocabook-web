package com.trocabook.Trocabook.model;

import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

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

    public Anuncio() {
    }

    public Anuncio(String id, String uidUsuario, String uidLivro, String nomeUsuario, TipoNegociacao tipoNegociacao, String titulo, String fotoPerfil, String capa, List<String> autores, List<String> categorias) {
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

    public enum TipoNegociacao {
        TROCA, VENDA, AMBOS
    }

    public static Anuncio from(AnuncioDTO anuncioDTO){
        return new Anuncio(
                anuncioDTO.id(),
                anuncioDTO.uidUsuario(),
                anuncioDTO.uidLivro(),
                anuncioDTO.nomeUsuario(),
                TipoNegociacao.valueOf(anuncioDTO.tipoNegociacao()),
                anuncioDTO.titulo(), anuncioDTO.fotoPerfil(),
                anuncioDTO.capa(), anuncioDTO.autores(),
                anuncioDTO.categorias());
    }

    public AnuncioDTO paraDto(){
        return new AnuncioDTO(
                this.id,
                this.uidUsuario,
                this.uidLivro,
                this.nomeUsuario,
                this.tipoNegociacao.name(),
                this.titulo,
                this.fotoPerfil,
                this.capa,
                this.autores,
                this.categorias
        );
    }

    private static String normalizarTitulo(String titulo) {
        if (titulo == null) {
            return "";
        }

        return titulo.toLowerCase(Locale.ROOT);
    }

}
