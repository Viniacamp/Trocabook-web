package com.trocabook.Trocabook.model;

import java.io.Serializable;

public class Autor implements Serializable {
    private String id;

    private String nome;

    public Autor() {

    }

    public Autor(String id, String nome) {
        this.id = id;
        this.nome = nome;
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
}
