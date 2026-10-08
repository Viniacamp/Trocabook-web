package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Anuncio;

import java.util.List;

public interface AnuncioRepository {

    Anuncio salvar(Anuncio anuncio);

    List<Anuncio> listarTodos();

    List<Anuncio> listarAtivos();

    List<Anuncio> listarTodosPorTipoNegociacao(
            Anuncio.TipoNegociacao tipoNegociacao,
            Anuncio.StatusAnuncio status
    );

    Anuncio buscarPorUid(String uid);

    List<Anuncio> buscarPorUidUsuario(
            String uidUsuario,
            Anuncio.StatusAnuncio status
    );

    List<Anuncio> buscarPorUidUsuarioETipoNegociacao(
            String uidUsuario,
            Anuncio.TipoNegociacao tipoNegociacao,
            Anuncio.StatusAnuncio status
    );

    List<Anuncio> buscarPorUidLivro(
            String uidLivro,
            Anuncio.StatusAnuncio status
    );

    List<Anuncio> buscarPorTitulo(
            String titulo,
            Anuncio.StatusAnuncio status
    );

    Anuncio atualizar(Anuncio anuncio);

    void deletar(String uid);
}