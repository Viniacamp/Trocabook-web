package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Anuncio;

import java.util.List;

public interface AnuncioRepository {
    Anuncio salvar(Anuncio anuncio);

    List<Anuncio> listarTodos();

    List<Anuncio> listarTodosPorTipoNegociacao(Anuncio.TipoNegociacao tipoNegociacao);

    Anuncio buscarPorUid(String uid);

    List<Anuncio> buscarPorUidUsuario(String uidUsuario);

    List<Anuncio> buscarPorUidUsuarioETipoNegociacao(String uidUsuario, Anuncio.TipoNegociacao tipoNegociacao);

    List<Anuncio> buscarPorUidLivro(String uidLivro);

    List<Anuncio> buscarPorTitulo(String titulo);

    Anuncio atualizar(Anuncio anuncio);

    void deletar(String uid);
}
