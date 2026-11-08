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
import jakarta.servlet.http.HttpSession;
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

    private static final String SESSION_USER_ROLE = "CACHED_USER_ROLE";
    private static final String SESSION_USER_UID = "CACHED_USER_UID";

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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            FirebaseToken firebaseToken = firebaseAuthService.validarToken(token);
            String uid = firebaseToken.getUid();
            HttpSession session = request.getSession(true);

            String roleName;
            String cachedUid = (String) session.getAttribute(SESSION_USER_UID);

            // 🚀 CACHE: Se os dados do usuário já estiverem na sessão para esse UID, evita consultar o Firestore
            if (cachedUid != null && cachedUid.equals(uid)) {
                roleName = (String) session.getAttribute(SESSION_USER_ROLE);
            } else {
                // Caso contrário, busca no Firestore apenas 1 vez e salva na sessão
                Usuario usuario = usuarioRepository.buscarPorUid(uid);

                if (usuario == null) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                roleName = usuario.getRole().name();
                session.setAttribute(SESSION_USER_UID, uid);
                session.setAttribute(SESSION_USER_ROLE, roleName);
            }

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority("ROLE_" + roleName);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            firebaseToken,
                            null,
                            List.of(authority)
                    );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(context, request, response);

        } catch (FirebaseAuthException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}