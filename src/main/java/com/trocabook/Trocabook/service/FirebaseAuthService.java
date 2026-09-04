package com.trocabook.Trocabook.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
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
}
