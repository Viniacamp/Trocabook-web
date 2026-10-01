package com.trocabook.Trocabook.service.feign;

import com.trocabook.Trocabook.controllers.request.RecomendacaoRequest;
import com.trocabook.Trocabook.controllers.response.RecomendacaoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "recomendacaoService",
        url = "${recomendacao.service.url}"
)
public interface RecomendacaoApiService {

    @PostMapping("/recomendacoes")
    List<RecomendacaoResponse> recomendar(
            @RequestBody RecomendacaoRequest request
    );
}
