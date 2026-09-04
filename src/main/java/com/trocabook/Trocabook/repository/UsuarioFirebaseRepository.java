package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.UsuarioFirebase;

import java.util.List;

public interface UsuarioFirebaseRepository {

    UsuarioFirebase cadastrar(UsuarioFirebase entidade);

    UsuarioFirebase buscarPorUid(String uid);

    List<UsuarioFirebase> buscarTodos();

    List<UsuarioFirebase> buscarPorUids(List<String> uids);

    UsuarioFirebase atualizar(UsuarioFirebase entidade);

    void deletar(String uid);

    UsuarioFirebase buscarPorEmail(String email);

    UsuarioFirebase buscarPorCpf(String cpf);


}