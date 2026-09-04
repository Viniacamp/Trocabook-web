package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Anuncio;

import java.util.List;

public interface AnuncioRepository {
    Anuncio salvar(Anuncio anuncio);

    Anuncio buscarPorUid(String uid);

    List<Anuncio> buscarPorUidUsuario(String uidUsuario);

    List<Anuncio> buscarPorUidLivro(String uidLivro);

    Anuncio atualizar(Anuncio anuncio);

    void deletar(String uid);
}
