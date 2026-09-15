package com.trocabook.Trocabook.service.impl;

import com.google.firebase.auth.FirebaseToken;
import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.repository.impl.UsuarioRepositoryImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    private static final String USUARIO_LOGADO = "usuarioLogado";

    private final UsuarioRepositoryImpl usuarioRepository;

    public UsuarioAutenticadoService(
            UsuarioRepositoryImpl usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuario(HttpSession sessao) {

        Usuario usuario =
                (Usuario) sessao.getAttribute(USUARIO_LOGADO);

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
            Usuario usuario
    ) {
        sessao.setAttribute(USUARIO_LOGADO, usuario);
    }

    public void limparSessao(HttpSession sessao) {
        sessao.removeAttribute(USUARIO_LOGADO);
    }

    public UsuarioOutput getUsuarioOutput(
            HttpSession sessao
    ) {

        Usuario usuario = getUsuario(sessao);

        return new UsuarioOutput(
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