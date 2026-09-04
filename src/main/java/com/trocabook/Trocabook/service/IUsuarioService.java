package com.trocabook.Trocabook.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseInput;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseOutput;

public interface IUsuarioService {
    void cadastrar(UsuarioFirebaseInput input) throws FirebaseAuthException;

    UsuarioFirebaseOutput logar(String uid);

    UsuarioFirebaseOutput atualizar(String uid, UsuarioFirebaseInput input);

    void deletar(String uid);

    boolean existeComEmail(String email);

    boolean existeComCpf(String cpf);
}
