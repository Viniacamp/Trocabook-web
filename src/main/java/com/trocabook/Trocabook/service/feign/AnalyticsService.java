package com.trocabook.Trocabook.service.feign;


import com.trocabook.Trocabook.controllers.request.AnuncioAnalyticsRequest;
import com.trocabook.Trocabook.controllers.request.NegociacaoAnalyticsRequest;
import com.trocabook.Trocabook.controllers.request.PrevisaoUsuariosAnalyticsRequest;
import com.trocabook.Trocabook.controllers.response.AnuncioResponse;
import com.trocabook.Trocabook.controllers.response.NegociacaoResponse;
import com.trocabook.Trocabook.controllers.response.PrevisaoTemporalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "analyticsService", url = "${analytics.service.url:http://localhost:8001}")
public interface AnalyticsService {

    @PostMapping("/analytics/anuncios")
    AnuncioResponse analisarAnuncios(@RequestBody AnuncioAnalyticsRequest request);

    @PostMapping("/analytics/negociacoes")
    NegociacaoResponse analisarNegociacoes(@RequestBody NegociacaoAnalyticsRequest request);

    @PostMapping("/analytics/previsao-temporal")
    PrevisaoTemporalResponse preverUsuarios(@RequestBody PrevisaoUsuariosAnalyticsRequest request);
}