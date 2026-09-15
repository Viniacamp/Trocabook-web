package com.trocabook.Trocabook.model;

import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;

import java.io.Serializable;
import java.util.List;

public class Livro implements Serializable {
    private String id;
    private String googleBooksId;
    private String titulo;
    private List<String> idsAutores;
    private List<String> idsCategorias;
    private String publicadora;
    private String dataPublicacao;
    private String urlImagem;

    public Livro() {
    }

    public Livro(String id, String googleBooksId, String titulo, List<String> idsAutores, List<String> idsCategorias, String publicadora, String dataPublicacao, String urlImagem) {
        this.id = id;
        this.googleBooksId = googleBooksId;
        this.titulo = titulo;
        this.idsAutores = idsAutores;
        this.idsCategorias = idsCategorias;
        this.publicadora = publicadora;
        this.dataPublicacao = dataPublicacao;
        this.urlImagem = urlImagem;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoogleBooksId() {
        return googleBooksId;
    }

    public void setGoogleBooksId(String googleBooksId) {
        this.googleBooksId = googleBooksId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<String> getIdsAutores() {
        return idsAutores;
    }

    public void setIdsAutores(List<String> idsAutores) {
        this.idsAutores = idsAutores;
    }

    public List<String> getIdsCategorias() {
        return idsCategorias;
    }

    public void setIdsCategorias(List<String> idsCategorias) {
        this.idsCategorias = idsCategorias;
    }

    public String getPublicadora() {
        return publicadora;
    }

    public void setPublicadora(String publicadora) {
        this.publicadora = publicadora;
    }

    public String getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(String dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public static Livro from(
            LivroBuscaOutput livro,
            List<String> idsAutores,
            List<String> idsCategorias
    ) {
        Livro entidade = new Livro();

        entidade.googleBooksId = livro.googleBooksId();
        entidade.titulo = livro.titulo();
        entidade.idsAutores = idsAutores;
        entidade.idsCategorias = idsCategorias;
        entidade.publicadora = livro.publicadora();
        entidade.dataPublicacao = livro.dataPublicacao();
        entidade.urlImagem = livro.urlImagem();

        return entidade;
    }
}
