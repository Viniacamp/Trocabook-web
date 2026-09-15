package com.trocabook.Trocabook.service.impl;

import com.google.firebase.auth.*;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthService(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    public UserRecord criarUsuario(String email, String senha) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(senha);

        return firebaseAuth.createUser(request);
    }

    public UserRecord buscarPorUid(String uid) throws FirebaseAuthException {
        return firebaseAuth.getUser(uid);
    }

    public void deletar(String uid) throws FirebaseAuthException {
        firebaseAuth.deleteUser(uid);
    }

    public FirebaseToken validarToken(String token)
            throws FirebaseAuthException {

        return firebaseAuth.verifyIdToken(token);
    }

    public String gerarLinkRedefinicaoSenha(String email)
            throws FirebaseAuthException {

        return firebaseAuth.generatePasswordResetLink(email);
    }
}
