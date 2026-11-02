package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.controllers.response.AnuncioResponse;
import com.trocabook.Trocabook.controllers.response.NegociacaoResponse;
import com.trocabook.Trocabook.controllers.response.PrevisaoTemporalResponse;
import com.trocabook.Trocabook.service.impl.AnaliseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsApiController {

    private final AnaliseService analiseService;

    public AdminAnalyticsApiController(
            AnaliseService analiseService
    ) {
        this.analiseService = analiseService;
    }

    @PostMapping("/anuncios")
    public ResponseEntity<AnuncioResponse> analisarAnuncios() {
        return ResponseEntity.ok(analiseService.analisarAnuncios());
    }

    @PostMapping("/negociacoes")
    public ResponseEntity<NegociacaoResponse> analisarNegociacoes() {

        return ResponseEntity.ok(analiseService.analisarNegociacoes());
    }

    @PostMapping("/previsao-temporal")
    public ResponseEntity<PrevisaoTemporalResponse> preverUsuarios() {
        return ResponseEntity.ok(analiseService.preverUsuarios());
    }
}