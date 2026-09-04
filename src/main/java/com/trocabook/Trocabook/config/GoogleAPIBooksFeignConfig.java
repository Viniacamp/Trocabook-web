package com.trocabook.Trocabook.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuração do <b>OpenFeign</b> responsável por adicionar a chave de API
 * da <b>Google Books API</b> às requisições realizadas pelo cliente Feign.
 *
 * <p>A chave é obtida a partir da propriedade {@code google.books.api.key},
 * permitindo que o valor não fique diretamente exposto no código-fonte.</p>
 *
 * @author PedroAmaralMa
 */
public class GoogleAPIBooksFeignConfig implements RequestInterceptor {
    /** Chave de acesso utilizada para autenticar as requisições da Google Books API. */
    @Value("${google.books.api.key}")
    private String chaveApi;

    /**
     * Adiciona a chave de API como parâmetro da requisição enviada pela
     * Google Books API.
     *
     * @param template template da requisição que será interceptada
     */
    @Override
    public void apply(RequestTemplate template) {
        template.query("key", chaveApi);
    }
}