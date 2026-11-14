package com.trocabook.Trocabook.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            FirebaseAuthenticationFilter firebaseAuthenticationFilter
    ) throws Exception {

        http
                .cors(withDefaults())

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/dados/**",
                                "/chat/**",
                                "/pesquisar/**",
                                "/api/auth/**",
                                "/api/fcm/**",
                                "/deslogar",
                                "/cadastro"
                        )
                )

                .securityContext(context -> context
                        .requireExplicitSave(false)
                )

                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/",
                                "/login",
                                "/loginNovo",
                                "/cadastro",
                                "/cadastroNovo",
                                "/sobreNos",
                                "/ajuda",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/uploads/**",
                                "/vendor/**",
                                "/dados/**",
                                "/api/auth/**",
                                "/esqueci-senha",
                                "/redefinir-senha",
                                "/firebase-messaging-sw.js"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        firebaseAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado");
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )

                .headers(headers -> headers
                        .frameOptions(frameOptions ->
                                frameOptions.sameOrigin()
                        )

                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +

                                                "script-src 'self' 'unsafe-inline' 'unsafe-eval' " +
                                                "https://vlibras.gov.br " +
                                                "https://www.google.com " +
                                                "https://www.gstatic.com " +
                                                "https://recaptcha.net " +
                                                "https://www.recaptcha.net " +
                                                "https://code.jquery.com " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://cdnjs.cloudflare.com; " +

                                                "worker-src 'self' blob:; " +

                                                "style-src 'self' 'unsafe-inline' " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://cdnjs.cloudflare.com " +
                                                "https://vlibras.gov.br " +
                                                "https://fonts.googleapis.com; " +

                                                "font-src 'self' data: " +
                                                "https://cdnjs.cloudflare.com " +
                                                "https://fonts.gstatic.com; " +

                                                "frame-src 'self' " +
                                                "https://www.google.com " +
                                                "https://recaptcha.google.com " +
                                                "https://www.recaptcha.net; " +

                                                "img-src 'self' data: blob: " +
                                                "https://*.azurewebsites.net " +
                                                "https://vlibras.gov.br " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://books.google.com " +
                                                "https://books.googleusercontent.com " +
                                                "https://books.google.com/books/content " +
                                                "https://www.gstatic.com " +
                                                "https://firebasestorage.googleapis.com; " +

                                                "connect-src 'self' " +
                                                "https://*.azurewebsites.net " +
                                                "wss://*.azurewebsites.net " +
                                                "https://trocabookchatservice.onrender.com " +
                                                "wss://trocabookchatservice.onrender.com " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://cdnjs.cloudflare.com " +
                                                "https://www.googleapis.com " +
                                                "https://www.gstatic.com " +
                                                "https://identitytoolkit.googleapis.com " +
                                                "https://securetoken.googleapis.com " +
                                                "https://fcmregistrations.googleapis.com " +
                                                "https://firebaseinstallations.googleapis.com " +
                                                "https://firebasestorage.googleapis.com " +
                                                "https://www.google.com " +
                                                "https://vlibras.gov.br " +
                                                "https://api.mymemory.translated.net; " +

                                                "form-action 'self'; " +
                                                "frame-ancestors 'self';"
                                )
                        )

                        .addHeaderWriter(new StaticHeadersWriter(
                                "Permissions-Policy", "camera=(), microphone=(), geolocation=()"
                        ))

                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )

                        .referrerPolicy(policy -> policy
                                .policy(
                                        ReferrerPolicyHeaderWriter.ReferrerPolicy
                                                .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                                )
                        )

                        .contentTypeOptions(withDefaults())
                );

        return http.build();
    }
}