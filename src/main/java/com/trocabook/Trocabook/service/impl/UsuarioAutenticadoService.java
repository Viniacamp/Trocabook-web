package com.trocabook.Trocabook.service.impl;

import com.google.firebase.auth.FirebaseToken;
import com.trocabook.Trocabook.model.UsuarioFirebase;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseOutput;
import com.trocabook.Trocabook.repository.impl.UsuarioFirebaseRepositoryImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    private static final String USUARIO_LOGADO = "usuarioLogado";

    private final UsuarioFirebaseRepositoryImpl usuarioRepository;

    public UsuarioAutenticadoService(
            UsuarioFirebaseRepositoryImpl usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioFirebase getUsuario(HttpSession sessao) {

        UsuarioFirebase usuario =
                (UsuarioFirebase) sessao.getAttribute(USUARIO_LOGADO);

        if (usuario != null) {
            return usuario;
        }

        FirebaseToken firebaseToken = getFirebaseToken();

        String uid = firebaseToken.getUid();

        usuario = usuarioRepository.buscarPorUid(uid);

        if (usuario == null){
            throw  new IllegalStateException(
                    "Usuário autenticado não encontrado."
            );
        }

        atualizarSessao(sessao, usuario);

        return usuario;
    }

    public void atualizarSessao(
            HttpSession sessao,
            UsuarioFirebase usuario
    ) {
        sessao.setAttribute(USUARIO_LOGADO, usuario);
    }

    public void limparSessao(HttpSession sessao) {
        sessao.removeAttribute(USUARIO_LOGADO);
    }

    public UsuarioFirebaseOutput getUsuarioOutput(
            HttpSession sessao
    ) {

        UsuarioFirebase usuario = getUsuario(sessao);

        return new UsuarioFirebaseOutput(
                usuario.getId(),
                usuario.getNome(),
                usuario.getFotoPerfil()
        );
    }

    private FirebaseToken getFirebaseToken() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal()
                        instanceof FirebaseToken)) {

            throw new IllegalStateException(
                    "Usuário não autenticado."
            );
        }

        return (FirebaseToken) authentication.getPrincipal();
    }

}