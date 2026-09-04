package com.trocabook.Trocabook.service.impl;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.trocabook.Trocabook.model.UsuarioFirebase;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseInput;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseOutput;
import com.trocabook.Trocabook.repository.UsuarioFirebaseRepository;
import com.trocabook.Trocabook.service.FirebaseAuthService;
import com.trocabook.Trocabook.service.IUsuarioService;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements IUsuarioService {
    private final UsuarioFirebaseRepository usuarioFirebaseRepository;

    private final FirebaseAuthService firebaseAuthService;

    public UsuarioService(UsuarioFirebaseRepository usuarioFirebaseRepository, FirebaseAuthService firebaseAuthService) {
        this.usuarioFirebaseRepository = usuarioFirebaseRepository;
        this.firebaseAuthService = firebaseAuthService;
    }

    @Override
    public void cadastrar(UsuarioFirebaseInput input) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuthService.criarUsuario(
                input.emailPrincipal(),
                input.senha()
        );

        UsuarioFirebase entidade = UsuarioFirebase.from(input);
        entidade.setId(userRecord.getUid());

        usuarioFirebaseRepository.cadastrar(entidade);

    }

    @Override
    public UsuarioFirebaseOutput logar(String uid) {
        UsuarioFirebase usuarioFirebase = usuarioFirebaseRepository.buscarPorUid(uid);

        if (usuarioFirebase == null){
            return null;
        }

        return usuarioFirebase.paraOutput();
    }

    @Override
    public UsuarioFirebaseOutput atualizar(String uid, UsuarioFirebaseInput input) {
        if (usuarioFirebaseRepository.buscarPorUid(uid) == null){
            return null;
        }

        UsuarioFirebase entidade = UsuarioFirebase.from(input);

        entidade.setId(uid);

        UsuarioFirebase entidadeSalva = usuarioFirebaseRepository.atualizar(entidade);
        return entidadeSalva.paraOutput();
    }


    @Override
    public void deletar(String uid) {
        if (usuarioFirebaseRepository.buscarPorUid(uid) != null){
            usuarioFirebaseRepository.deletar(uid);
        }
    }

    @Override
    public boolean existeComEmail(String email) {
        return usuarioFirebaseRepository.buscarPorEmail(email) != null;
    }

    @Override
    public boolean existeComCpf(String cpf) {
        return usuarioFirebaseRepository.buscarPorCpf(cpf) != null;
    }
}
