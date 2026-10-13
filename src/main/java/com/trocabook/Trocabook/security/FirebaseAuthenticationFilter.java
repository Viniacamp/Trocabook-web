package com.trocabook.Trocabook.security;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.repository.UsuarioRepository;
import com.trocabook.Trocabook.service.impl.FirebaseAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private final FirebaseAuthService firebaseAuthService;
    private final UsuarioRepository usuarioRepository;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public FirebaseAuthenticationFilter(
            FirebaseAuthService firebaseAuthService,
            UsuarioRepository usuarioRepository
    ) {
        this.firebaseAuthService = firebaseAuthService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        /*
         * Se não houver token Firebase, deixa o Spring Security
         * utilizar normalmente uma autenticação já existente
         * na HttpSession.
         */
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {

            /*
             * Valida o token e obtém a identidade autenticada
             * pelo Firebase Authentication.
             */
            FirebaseToken firebaseToken =
                    firebaseAuthService.validarToken(token);

            String uid = firebaseToken.getUid();

            /*
             * Busca os dados de autorização no Firestore.
             */
            Usuario usuario =
                    usuarioRepository.buscarPorUid(uid);

            if (usuario == null) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            /*
             * Converte a role do Trocabook para uma authority
             * reconhecida pelo Spring Security.
             *
             * USER  -> ROLE_USER
             * ADMIN -> ROLE_ADMIN
             */
            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(
                            "ROLE_" + usuario.getRole().name()
                    );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            firebaseToken,
                            null,
                            List.of(authority)
                    );

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            /*
             * Persiste a autenticação na sessão para a aplicação Web.
             */
            securityContextRepository.saveContext(
                    context,
                    request,
                    response
            );

        } catch (FirebaseAuthException e) {

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}