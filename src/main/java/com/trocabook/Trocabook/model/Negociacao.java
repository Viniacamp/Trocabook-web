package com.trocabook.Trocabook.model;

import com.trocabook.Trocabook.model.dto.NegociacaoDTO;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Negociacao implements Serializable {

    private String id;

    private String usuarioAnuncianteId;

    private String usuarioCompradorId;

    private String anuncioId;

    private String dataNegociacao;

    private TipoNegociacao tipoNegociacao;

    private String nmAnunciante;

    private String fotoPerfilAnunciante;

    private String nmComprador;

    private String fotoPerfilComprador;

    private String titulo;

    private String capa;

    private StatusNegociacao status;

    private boolean confirmacaoAnunciante;

    private boolean confirmacaoComprador;

    private String anuncioOferecidoId;
    private String tituloLivroOferecido;
    private String capaLivroOferecido;
    private String descricaoOferta;

    public Negociacao() {
    }

    public Negociacao(String id, String usuarioAnuncianteId, String usuarioCompradorId, String anuncioId, String dataNegociacao, TipoNegociacao tipoNegociacao, StatusNegociacao statusNegociacao, String nmAnunciante, String fotoPerfilAnunciante, String nmComprador, String fotoPerfilComprador, String titulo, String capa, boolean confirmacaoAnunciante, boolean confirmacaoComprador, String anuncioOferecidoId, String tituloLivroOferecido, String capaLivroOferecido, String descricaoOferta) {
        this.id = id;
        this.usuarioAnuncianteId = usuarioAnuncianteId;
        this.usuarioCompradorId = usuarioCompradorId;
        this.anuncioId = anuncioId;
        this.dataNegociacao = dataNegociacao;
        this.tipoNegociacao = tipoNegociacao;
        this.status = statusNegociacao;
        this.nmAnunciante = nmAnunciante;
        this.fotoPerfilAnunciante = fotoPerfilAnunciante;
        this.nmComprador = nmComprador;
        this.fotoPerfilComprador = fotoPerfilComprador;
        this.titulo = titulo;
        this.capa = capa;
        this.confirmacaoAnunciante = confirmacaoAnunciante;
        this.confirmacaoComprador = confirmacaoComprador;
        this.anuncioOferecidoId = anuncioOferecidoId;
        this.tituloLivroOferecido = tituloLivroOferecido;
        this.capaLivroOferecido = capaLivroOferecido;
        this.descricaoOferta = descricaoOferta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioAnuncianteId() {
        return usuarioAnuncianteId;
    }

    public void setUsuarioAnuncianteId(String usuarioAnuncianteId) {
        this.usuarioAnuncianteId = usuarioAnuncianteId;
    }

    public String getUsuarioCompradorId() {
        return usuarioCompradorId;
    }

    public void setUsuarioCompradorId(String usuarioCompradorId) {
        this.usuarioCompradorId = usuarioCompradorId;
    }

    public String getAnuncioId() {
        return anuncioId;
    }

    public void setAnuncioId(String anuncioId) {
        this.anuncioId = anuncioId;
    }

    public String getDataNegociacao() {
        return dataNegociacao;
    }

    public void setDataNegociacao(String dataNegociacao) {
        this.dataNegociacao = dataNegociacao;
    }

    public TipoNegociacao getTipoNegociacao() {
        return tipoNegociacao;
    }

    public void setTipoNegociacao(TipoNegociacao tipoNegociacao) {
        this.tipoNegociacao = tipoNegociacao;
    }

    public String getNmAnunciante() {
        return nmAnunciante;
    }

    public void setNmAnunciante(String nmAnunciante) {
        this.nmAnunciante = nmAnunciante;
    }

    public String getFotoPerfilAnunciante() {
        return fotoPerfilAnunciante;
    }

    public void setFotoPerfilAnunciante(String fotoPerfilAnunciante) {
        this.fotoPerfilAnunciante = fotoPerfilAnunciante;
    }

    public String getNmComprador() {
        return nmComprador;
    }

    public void setNmComprador(String nmComprador) {
        this.nmComprador = nmComprador;
    }

    public String getFotoPerfilComprador() {
        return fotoPerfilComprador;
    }

    public void setFotoPerfilComprador(String fotoPerfilComprador) {
        this.fotoPerfilComprador = fotoPerfilComprador;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCapa() {
        return capa;
    }

    public void setCapa(String capa) {
        this.capa = capa;
    }

    public boolean isConfirmacaoAnunciante() {
        return confirmacaoAnunciante;
    }

    public void setConfirmacaoAnunciante(boolean confirmacaoAnunciante) {
        this.confirmacaoAnunciante = confirmacaoAnunciante;
    }

    public boolean isConfirmacaoComprador() {
        return confirmacaoComprador;
    }

    public void setConfirmacaoComprador(boolean confirmacaoComprador) {
        this.confirmacaoComprador = confirmacaoComprador;
    }

    public StatusNegociacao getStatus() {
        return status;
    }

    public void setStatus(StatusNegociacao status) {
        this.status = status;
    }

    public String getAnuncioOferecidoId() {
        return anuncioOferecidoId;
    }

    public void setAnuncioOferecidoId(String anuncioOferecidoId) {
        this.anuncioOferecidoId = anuncioOferecidoId;
    }

    public String getTituloLivroOferecido() {
        return tituloLivroOferecido;
    }

    public void setTituloLivroOferecido(String tituloLivroOferecido) {
        this.tituloLivroOferecido = tituloLivroOferecido;
    }

    public String getCapaLivroOferecido() {
        return capaLivroOferecido;
    }

    public void setCapaLivroOferecido(String capaLivroOferecido) {
        this.capaLivroOferecido = capaLivroOferecido;
    }

    public String getDescricaoOferta() {
        return descricaoOferta;
    }

    public void setDescricaoOferta(String descricaoOferta) {
        this.descricaoOferta = descricaoOferta;
    }

    public enum StatusNegociacao {
        PENDENTE,
        EM_ANDAMENTO,
        RECUSADA,
        CANCELADA,
        FINALIZADA
    }

    public enum TipoNegociacao {
        TROCA, VENDA, AMBOS
    }

    public static Negociacao from(NegociacaoDTO negociacaoDTO) {
        return new Negociacao(
                negociacaoDTO.id(),
                negociacaoDTO.usuarioAnuncianteId(),
                negociacaoDTO.usuarioCompradorId(),
                negociacaoDTO.anuncioId(),
                negociacaoDTO.dataNegociacao().toString(),
                TipoNegociacao.valueOf(negociacaoDTO.tipoNegociacao()),
                StatusNegociacao.valueOf(negociacaoDTO.status()),
                negociacaoDTO.nmAnunciante(),
                negociacaoDTO.fotoPerfilAnunciante(),
                negociacaoDTO.nmComprador(),
                negociacaoDTO.fotoPerfilComprador(),
                negociacaoDTO.titulo(),
                negociacaoDTO.capa(),
                negociacaoDTO.confirmacaoAnunciante(),
                negociacaoDTO.confirmacaoComprador(),
                negociacaoDTO.anuncioOferecidoId(),
                negociacaoDTO.tituloLivroOferecido(),
                negociacaoDTO.capaLivroOferecido(),
                negociacaoDTO.descricaoOferta()
        );
    }

    public NegociacaoDTO paraDto() {
        return new NegociacaoDTO(
                this.id,
                this.usuarioAnuncianteId,
                this.usuarioCompradorId,
                this.anuncioId,
                LocalDateTime.parse(this.dataNegociacao),
                this.tipoNegociacao.name(),
                this.status.name(),
                this.nmAnunciante,
                this.fotoPerfilAnunciante,
                this.nmComprador,
                this.fotoPerfilComprador,
                this.titulo,
                this.capa,
                this.confirmacaoAnunciante,
                this.confirmacaoComprador,
                this.anuncioOferecidoId,
                this.tituloLivroOferecido,
                this.capaLivroOferecido,
                this.descricaoOferta
        );
    }
}
