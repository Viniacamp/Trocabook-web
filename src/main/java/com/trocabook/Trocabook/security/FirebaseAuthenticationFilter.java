package com.trocabook.Trocabook.security;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.trocabook.Trocabook.service.impl.FirebaseAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public FirebaseAuthenticationFilter(
            FirebaseAuthService firebaseAuthService) {

        this.firebaseAuthService = firebaseAuthService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        /*
         * Se não houver token Firebase,
         * deixa o Spring Security continuar normalmente.
         *
         * Isso permite que uma autenticação já existente
         * na HttpSession seja utilizada.
         */
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {

            /*
             * Valida o token recebido do Firebase.
             */
            FirebaseToken firebaseToken =
                    firebaseAuthService.validarToken(token);

            /*
             * Cria a autenticação do Spring Security.
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            firebaseToken,
                            null,
                            List.of()
                    );

            /*
             * Cria um novo SecurityContext.
             */
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            /*
             * Coloca a autenticação no contexto atual.
             */
            SecurityContextHolder.setContext(context);

            /*
             * Salva o SecurityContext na HttpSession.
             *
             * Assim, as próximas requisições da aplicação Web
             * poderão utilizar a autenticação sem precisar
             * receber novamente o Firebase Token.
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

