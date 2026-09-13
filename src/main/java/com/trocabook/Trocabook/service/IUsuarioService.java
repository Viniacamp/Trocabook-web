package com.trocabook.Trocabook.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.trocabook.Trocabook.model.dto.UsuarioInput;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;

import java.util.List;

public interface IUsuarioService {
    void cadastrar(UsuarioInput input) throws FirebaseAuthException;

    UsuarioOutput buscarPorUid(String uid);

    UsuarioOutput atualizar(String uid, UsuarioInput input);

    void deletar(String uid);

    boolean existeComEmail(String email);

    boolean existeComCpf(String cpf);

    UsuarioOutput buscarPorEmail(String email);

    List<UsuarioOutput> buscarMelhoresAvaliados();

    List<UsuarioOutput> buscarTodos();
}
