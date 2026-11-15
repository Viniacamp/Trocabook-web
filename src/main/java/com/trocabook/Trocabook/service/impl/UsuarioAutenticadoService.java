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
        // 1. Tenta pegar da sessão HTTP primeiro
        Usuario usuario = (Usuario) sessao.getAttribute(USUARIO_LOGADO);
        if (usuario != null) {
            return usuario;
        }

        // 2. Se não estiver na sessão, tenta recuperar via Token do Firebase
        try {
            FirebaseToken firebaseToken = getFirebaseToken();
            if (firebaseToken != null) {
                String uid = firebaseToken.getUid();
                usuario = usuarioRepository.buscarPorUid(uid);

                if (usuario != null) {
                    atualizarSessao(sessao, usuario);
                    return usuario;
                }
            }
        } catch (Exception e) {
            // Se falhar a extração do token, apenas retorna null em vez de quebrar a aplicação
            return null;
        }

        return null;
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

        if (usuario == null) {
            return null; // Retorna null para o Controller tratar com elegância
        }

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
            return null; // Retorna null em vez de lançar exceção travada
        }

        return (FirebaseToken) authentication.getPrincipal();
    }
}