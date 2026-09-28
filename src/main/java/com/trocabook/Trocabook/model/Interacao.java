package com.trocabook.Trocabook.model;

import com.google.cloud.Timestamp;
import com.trocabook.Trocabook.model.dto.InteracaoDTO;

import java.io.Serializable;

public class Interacao implements Serializable {

    private String id;

    private String uidUsuario;

    private TipoInteracao tipoInteracao;

    private String uidLivro;

    private String uidAnuncio;

    private String termoPesquisa;

    private OrigemInteracao origem;

    private Timestamp dataInteracao;

    public Interacao() {
    }

    public Interacao(
            String id,
            String uidUsuario,
            TipoInteracao tipoInteracao,
            String uidLivro,
            String uidAnuncio,
            String termoPesquisa,
            OrigemInteracao origem,
            Timestamp dataInteracao
    ) {
        this.id = id;
        this.uidUsuario = uidUsuario;
        this.tipoInteracao = tipoInteracao;
        this.uidLivro = uidLivro;
        this.uidAnuncio = uidAnuncio;
        this.termoPesquisa = termoPesquisa;
        this.origem = origem;
        this.dataInteracao = dataInteracao;
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

    public TipoInteracao getTipoInteracao() {
        return tipoInteracao;
    }

    public void setTipoInteracao(TipoInteracao tipoInteracao) {
        this.tipoInteracao = tipoInteracao;
    }

    public String getUidLivro() {
        return uidLivro;
    }

    public void setUidLivro(String uidLivro) {
        this.uidLivro = uidLivro;
    }

    public String getUidAnuncio() {
        return uidAnuncio;
    }

    public void setUidAnuncio(String uidAnuncio) {
        this.uidAnuncio = uidAnuncio;
    }

    public String getTermoPesquisa() {
        return termoPesquisa;
    }

    public void setTermoPesquisa(String termoPesquisa) {
        this.termoPesquisa = termoPesquisa;
    }

    public OrigemInteracao getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemInteracao origem) {
        this.origem = origem;
    }

    public Timestamp getDataInteracao() {
        return dataInteracao;
    }

    public void setDataInteracao(Timestamp dataInteracao) {
        this.dataInteracao = dataInteracao;
    }

    public enum TipoInteracao {
        PESQUISA,
        VISUALIZACAO,
        INICIO_CONVERSA
    }

    public enum OrigemInteracao {
        WEB,
        MOBILE
    }

    public static Interacao from(InteracaoDTO interacaoDTO) {
        return new Interacao(
                interacaoDTO.id(),
                interacaoDTO.uidUsuario(),
                interacaoDTO.tipoInteracao(),
                interacaoDTO.uidLivro(),
                interacaoDTO.uidAnuncio(),
                interacaoDTO.termoPesquisa(),
                interacaoDTO.origem(),
                interacaoDTO.dataInteracao()
        );
    }

    public InteracaoDTO paraDto() {
        return new InteracaoDTO(
                this.id,
                this.uidUsuario,
                this.tipoInteracao,
                this.uidLivro,
                this.uidAnuncio,
                this.termoPesquisa,
                this.origem,
                this.dataInteracao
        );
    }
}