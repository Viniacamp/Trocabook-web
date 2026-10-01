package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.controllers.request.RecomendacaoRequest;
import com.trocabook.Trocabook.controllers.response.RecomendacaoResponse;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.InteracaoDTO;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.IInteracaoService;
import com.trocabook.Trocabook.service.IRecomendacaoCacheService;
import com.trocabook.Trocabook.service.feign.RecomendacaoApiService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecomendacaoCacheService
        implements IRecomendacaoCacheService {

    private final RecomendacaoApiService recomendacaoApiService;
    private final IAnuncioService anuncioService;
    private final IInteracaoService interacaoService;

    public RecomendacaoCacheService(
            RecomendacaoApiService recomendacaoApiService,
            IAnuncioService anuncioService,
            IInteracaoService interacaoService
    ) {
        this.recomendacaoApiService = recomendacaoApiService;
        this.anuncioService = anuncioService;
        this.interacaoService = interacaoService;
    }

    @Override
    @Cacheable(
            value = "recomendacoes",
            key = "#uidUsuario"
    )
    public List<RecomendacaoResponse> buscarRanking(
            String uidUsuario
    ) {
        List<AnuncioDTO> anuncios =
                anuncioService.listarTodos();

        if (anuncios.isEmpty()) {
            return List.of();
        }

        List<InteracaoDTO> interacoes =
                interacaoService.listarPorUsuario(uidUsuario);

        RecomendacaoRequest request =
                new RecomendacaoRequest(
                        uidUsuario,
                        anuncios,
                        interacoes
                );

        return recomendacaoApiService.recomendar(request);
    }
}