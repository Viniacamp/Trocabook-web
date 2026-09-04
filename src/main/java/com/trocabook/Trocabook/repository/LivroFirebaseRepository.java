package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.LivroFirebase;

import java.util.List;

public interface LivroFirebaseRepository {

    LivroFirebase salvar(LivroFirebase entidade);

    LivroFirebase buscarPorUid(String uid);

    List<LivroFirebase> buscarTodos();

    List<LivroFirebase> buscarPorTitulo(String titulo);

    LivroFirebase buscarPorGoogleBooksId(String googleBooksId);
}
