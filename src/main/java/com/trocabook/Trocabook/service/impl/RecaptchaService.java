package com.trocabook.Trocabook.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.trocabook.Trocabook.model.dto.RecaptchaResponse;

@Service
public class RecaptchaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecaptchaService.class);

    // URL da API de verificação do Google
    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private static final double RECAPTCHA_SCORE_THRESHOLD = 0.5;

    // Injeta a chave secreta do seu arquivo application.properties
    @Value("${google.recaptcha.key.secret}")
    private String recaptchaSecret;

    public boolean verifyRecaptcha(String token) {
        // Se o token estiver vazio ou nulo, a validação falha imediatamente.
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = RECAPTCHA_VERIFY_URL + "?secret=" + recaptchaSecret + "&response=" + token;

            // Faz uma requisição POST para a API do Google e espera uma resposta que será mapeada para a classe RecaptchaResponse.
            RecaptchaResponse response = restTemplate.postForObject(url, null, RecaptchaResponse.class);

            if (response == null) {
                LOGGER.error("Resposta do reCAPTCHA foi nula.");
                return false;
            }

            LOGGER.info("Resposta do reCAPTCHA: success={}, score={}", response.isSuccess(), response.getScore());

            // A verificação é válida se a API do Google retornar sucesso E a pontuação for maior ou igual ao nosso limite.
            return response.isSuccess() && response.getScore() >= RECAPTCHA_SCORE_THRESHOLD;

        } catch (Exception e) {
            // Em caso de erro de rede ou qualquer outra exceção, registra o erro e retorna false.
            LOGGER.error("Erro ao verificar o reCAPTCHA: {}", e.getMessage());
            return false;
        }
    }
}