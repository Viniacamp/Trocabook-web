package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Categoria;

import java.util.List;

public interface CategoriaRepository {
    Categoria salvar(Categoria categoria);

    Categoria buscarPorUid(String uid);

    Categoria buscarPorNome(String nome);

    List<Categoria> buscarTodos();
}
