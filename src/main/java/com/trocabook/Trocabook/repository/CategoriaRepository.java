package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Categoria;

public interface CategoriaRepository {
    Categoria salvar(Categoria categoria);

    Categoria buscarPorUid(String uid);

    Categoria buscarPorNome(String nome);
}
