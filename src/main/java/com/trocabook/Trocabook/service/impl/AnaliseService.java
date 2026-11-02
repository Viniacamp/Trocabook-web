package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.controllers.request.AnuncioAnalyticsRequest;
import com.trocabook.Trocabook.controllers.request.NegociacaoAnalyticsRequest;
import com.trocabook.Trocabook.controllers.request.PrevisaoUsuariosAnalyticsRequest;
import com.trocabook.Trocabook.controllers.response.AnuncioResponse;
import com.trocabook.Trocabook.controllers.response.NegociacaoResponse;
import com.trocabook.Trocabook.controllers.response.PrevisaoTemporalResponse;
import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.model.dto.AnuncioAnalyticsDTO;
import com.trocabook.Trocabook.model.dto.NegociacaoAnalyticsDTO;
import com.trocabook.Trocabook.model.dto.UsuarioAnalyticsDTO;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.INegociacaoService;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.feign.AnalyticsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnaliseService {

    private final INegociacaoService negociacaoService;

    private final IAnuncioService anuncioService;

    private final IUsuarioService usuarioService;

    private final AnalyticsService analyticsService;

    public AnaliseService(INegociacaoService negociacaoService, IAnuncioService anuncioService, IUsuarioService usuarioService, AnalyticsService analyticsService) {
        this.negociacaoService = negociacaoService;
        this.anuncioService = anuncioService;
        this.usuarioService = usuarioService;
        this.analyticsService = analyticsService;
    }

    public AnuncioResponse analisarAnuncios(){
        List<AnuncioAnalyticsDTO> anuncioAnalyticsDTOS = anuncioService.listarParaAnalise();
        AnuncioAnalyticsRequest anuncioAnalyticsRequest = new AnuncioAnalyticsRequest(anuncioAnalyticsDTOS);
        return analyticsService.analisarAnuncios(anuncioAnalyticsRequest);
    }

    public NegociacaoResponse analisarNegociacoes(){
        List<NegociacaoAnalyticsDTO> negociacaoAnalyticsDTOS = negociacaoService.listarAnalises();
        NegociacaoAnalyticsRequest negociacaoAnalyticsRequest = new NegociacaoAnalyticsRequest(negociacaoAnalyticsDTOS);
        return analyticsService.analisarNegociacoes(negociacaoAnalyticsRequest);
    }

    public PrevisaoTemporalResponse preverUsuarios(){
        List<UsuarioAnalyticsDTO> usuarioAnalyticsDTOS = usuarioService.buscarTodasDataCriacao(Usuario.Role.USER);
        PrevisaoUsuariosAnalyticsRequest previsaoUsuariosAnalyticsRequest = new PrevisaoUsuariosAnalyticsRequest(usuarioAnalyticsDTOS);
        return analyticsService.preverUsuarios(previsaoUsuariosAnalyticsRequest);
    }
}
