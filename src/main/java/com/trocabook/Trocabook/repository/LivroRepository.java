package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Livro;

import java.util.List;

public interface LivroRepository {

    Livro salvar(Livro entidade);

    Livro buscarPorUid(String uid);

    List<Livro> buscarTodos();

    List<Livro> buscarPorTitulo(String titulo);

    Livro buscarPorGoogleBooksId(String googleBooksId);
}
