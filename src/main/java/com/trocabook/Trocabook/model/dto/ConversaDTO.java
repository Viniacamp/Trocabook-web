package com.trocabook.Trocabook.model.dto;

public class ConversaDTO {

    private String uidAnuncio;
    private String uidDestinatario;
    private String nomeDestinatario;
    private String fotoDestinatario;
    private String ultimaMensagem;
    private boolean enviadaPeloUsuarioLogado;

    public String getUidAnuncio() {
        return uidAnuncio;
    }

    public void setUidAnuncio(String uidAnuncio) {
        this.uidAnuncio = uidAnuncio;
    }

    public String getUidDestinatario() {
        return uidDestinatario;
    }

    public void setUidDestinatario(String uidDestinatario) {
        this.uidDestinatario = uidDestinatario;
    }

    public String getNomeDestinatario() {
        return nomeDestinatario;
    }

    public void setNomeDestinatario(String nomeDestinatario) {
        this.nomeDestinatario = nomeDestinatario;
    }

    public String getFotoDestinatario() {
        return fotoDestinatario;
    }

    public void setFotoDestinatario(String fotoDestinatario) {
        this.fotoDestinatario = fotoDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public boolean isEnviadaPeloUsuarioLogado() {
        return enviadaPeloUsuarioLogado;
    }

    public void setEnviadaPeloUsuarioLogado(boolean enviadaPeloUsuarioLogado) {
        this.enviadaPeloUsuarioLogado = enviadaPeloUsuarioLogado;
    }
}