package com.trocabook.Trocabook.config;

import feign.RequestInterceptor;
import feign.RetryableException;
import feign.Retryer;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;


import java.io.IOException;


public class GoogleAPIBooksFeignConfig {

    @Value("${google.books.api.key}")
    private String chaveApi;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> template.query("key", chaveApi);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 3000, 3);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();

            // Erros transitórios de rede ou servidor
            if (status == 429 || status == 500 || status == 502 || status == 503 || status == 504) {
                return new RetryableException(
                        status,
                        "Erro transitório na Google Books API (HTTP " + status + ")",
                        response.request().httpMethod(),
                        (Long) null,
                        response.request()
                );
            }

            // Lê o array de bytes com tratamento de IOException
            byte[] bodyBytes = new byte[0];
            if (response.body() != null) {
                try {
                    bodyBytes = Util.toByteArray(response.body().asInputStream());
                } catch (IOException ignored) {
                }
            }

            return new feign.FeignException.FeignClientException(
                    status,
                    "Erro ao consultar Google Books API: HTTP " + status,
                    response.request(),
                    bodyBytes,
                    response.headers()
            );
        };
    }
}