package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Autor;

public interface AutorRepository {
    Autor salvar(Autor autor);
    Autor buscarPorUid(String uid);

    Autor buscarPorNome(String nome);
}
