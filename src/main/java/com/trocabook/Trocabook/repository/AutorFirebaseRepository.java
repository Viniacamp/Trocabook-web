package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.AutorFirebase;

public interface AutorFirebaseRepository {
    AutorFirebase salvar(AutorFirebase autorFirebase);
    AutorFirebase buscarPorUid(String uid);

    AutorFirebase buscarPorNome(String nome);
}
