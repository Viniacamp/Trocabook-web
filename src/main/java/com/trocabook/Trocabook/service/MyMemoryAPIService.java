package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.dto.MyMemoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client HTTP, criado via <b>OpenFeign</b>, para o consumo da
 * <b>MyMemory API</b>.
 *
 * <p>Responsável por realizar solicitações de tradução de textos,
 * utilizando os idiomas de origem e destino informados.</p>
 *
 * @see <a href="https://spring.io/projects/spring-cloud-openfeign">Spring Cloud OpenFeign</a>
 * @see <a href="https://mymemory.translated.net/doc/spec.php">MyMemory API</a>
 *
 * @author PedroAmaralMa
 */
@FeignClient(name = "mymemoryAPI", url = "https://api.mymemory.translated.net")
public interface MyMemoryAPIService {

    /**
     * Realiza uma solicitação de tradução utilizando a MyMemory API.
     *
     * @param texto texto que será traduzido
     * @param linguaOriginalTraducao par de idiomas utilizado na tradução,
     *                               no formato idiomaOrigem|idiomaDestino
     * @return resposta da MyMemory API contendo o resultado da tradução
     */
    @GetMapping("/get")
    MyMemoryResponse traduzir(@RequestParam("q") String texto, @RequestParam("langpair") String linguaOriginalTraducao);
}
