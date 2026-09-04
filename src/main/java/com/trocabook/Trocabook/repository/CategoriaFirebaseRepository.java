package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.CategoriaFirebase;

public interface CategoriaFirebaseRepository {
    CategoriaFirebase salvar(CategoriaFirebase categoriaFirebase);

    CategoriaFirebase buscarPorUid(String uid);

    CategoriaFirebase buscarPorNome(String nome);
}
