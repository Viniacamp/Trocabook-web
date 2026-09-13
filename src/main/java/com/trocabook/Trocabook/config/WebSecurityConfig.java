package com.trocabook.Trocabook.config;

import com.trocabook.Trocabook.security.FirebaseAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
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
                                "/pesquisar",
                                "/api/auth/**",
                                "/deslogar"
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
                                "/vendor/**",
                                "/dados/**",
                                "/esqueci-senha",
                                "/redefinir-senha"
                        ).permitAll()

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

                .headers(headers -> headers
                        .frameOptions(frameOptions ->
                                frameOptions.sameOrigin()
                        )

                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +

                                                "script-src 'self' 'unsafe-inline' " +
                                                "https://vlibras.gov.br " +
                                                "https://www.google.com " +
                                                "https://www.gstatic.com " +
                                                "https://code.jquery.com " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://cdnjs.cloudflare.com; " +

                                                "style-src 'self' 'unsafe-inline' " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://cdnjs.cloudflare.com " +
                                                "https://vlibras.gov.br " +
                                                "https://fonts.googleapis.com; " +

                                                "font-src 'self' " +
                                                "https://cdnjs.cloudflare.com " +
                                                "https://fonts.gstatic.com; " +

                                                "frame-src 'self' " +
                                                "https://www.google.com; " +

                                                "img-src 'self' data: " +
                                                "https://vlibras.gov.br " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://books.google.com " +
                                                "https://books.googleusercontent.com " +
                                                "https://books.google.com/books/content; " +

                                                "connect-src 'self' " +
                                                "http://localhost:8181 " +
                                                "https://trocabookchatservice.onrender.com " +
                                                "https://cdn.jsdelivr.net " +
                                                "https://www.googleapis.com " +
                                                "https://www.gstatic.com " +
                                                "https://identitytoolkit.googleapis.com " +
                                                "https://securetoken.googleapis.com " +
                                                "https://www.google.com " +
                                                "https://vlibras.gov.br " +
                                                "https://api.mymemory.translated.net; " +

                                                "form-action 'self'; " +
                                                "frame-ancestors 'self';"
                                )
                        )

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