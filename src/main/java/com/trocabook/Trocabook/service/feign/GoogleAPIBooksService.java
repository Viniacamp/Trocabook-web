package com.trocabook.Trocabook.service.feign;

import com.trocabook.Trocabook.config.GoogleAPIBooksFeignConfig;
import com.trocabook.Trocabook.model.dto.GoogleBooksResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client HTTP, criado via <b>OpenFeign</b>, para o consumo da
 * <b>Google Books API</b>.
 *
 * <p>Responsável por realizar consultas de livros e obter informações
 * disponibilizadas pela API do Google Books.</p>
 *
 * @see <a href="https://spring.io/projects/spring-cloud-openfeign">Spring Cloud OpenFeign</a>
 * @see <a href="https://developers.google.com/books">Google Books API</a>
 *
 * @author PedroAmaralMa
 */
@FeignClient(name = "googleBooks", url = "https://www.googleapis.com/books/v1/volumes", configuration = GoogleAPIBooksFeignConfig.class)
public interface GoogleAPIBooksService {

    /**
     * Realiza uma consulta na Google Books API a partir do título informado.
     *
     * @param titulo título ou termo utilizado na busca
     * @return resposta da Google Books API contendo os dados dos livros encontrados
     */
    @GetMapping
    GoogleBooksResponse buscarTitulo(@RequestParam("q") String titulo);
}
