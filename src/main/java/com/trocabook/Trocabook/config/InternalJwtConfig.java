package com.trocabook.Trocabook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class InternalJwtConfig {

    @Value("${jwt.admin.public.key.path}")
    private String publicKeyPath;

    @Bean
    public RSAPublicKey adminServicePublicKey() throws Exception {

        String publicKeyPem =
                Files.readString(Path.of(publicKeyPath));

        String publicKeyContent = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] publicKeyBytes =
                Base64.getDecoder().decode(publicKeyContent);

        X509EncodedKeySpec keySpec =
                new X509EncodedKeySpec(publicKeyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        return (RSAPublicKey)
                keyFactory.generatePublic(keySpec);
    }

    @Bean
    public JwtDecoder adminServiceJwtDecoder(
            RSAPublicKey adminServicePublicKey
    ) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(adminServicePublicKey)
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(
                        "trocabook-admin-service"
                );

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        audience ->
                                audience != null
                                        && audience.contains("trocabook-api")
                );

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                );

        decoder.setJwtValidator(validator);

        return decoder;
    }
}