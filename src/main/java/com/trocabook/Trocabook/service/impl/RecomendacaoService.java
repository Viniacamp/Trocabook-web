package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.controllers.response.RecomendacaoResponse;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.service.IRecomendacaoCacheService;
import com.trocabook.Trocabook.service.IRecomendacaoService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecomendacaoService implements IRecomendacaoService {

    private final IRecomendacaoCacheService recomendacaoCacheService;

    public RecomendacaoService(
            IRecomendacaoCacheService recomendacaoCacheService
    ) {
        this.recomendacaoCacheService = recomendacaoCacheService;
    }

    @Override
    public List<AnuncioDTO> buscarRecomendacoes(
            String uidUsuario,
            int quantidade,
            List<AnuncioDTO> anuncios
    ) {
        if (uidUsuario == null || uidUsuario.isBlank()) {
            return List.of();
        }

        if (anuncios == null || anuncios.isEmpty()) {
            return List.of();
        }

        List<RecomendacaoResponse> ranking =
                recomendacaoCacheService.buscarRanking(uidUsuario);

        if (ranking == null || ranking.isEmpty()) {
            return buscarAleatorios(
                    anuncios,
                    quantidade > 0
                            ? quantidade
                            : anuncios.size()
            );
        }

        Map<String, AnuncioDTO> anunciosPorId =
                anuncios.stream()
                        .collect(Collectors.toMap(
                                AnuncioDTO::id,
                                Function.identity()
                        ));

        var recomendacoes = ranking.stream()
                .map(RecomendacaoResponse::uidAnuncio)
                .map(anunciosPorId::get)
                .filter(Objects::nonNull);

        if (quantidade > 0) {
            recomendacoes =
                    recomendacoes.limit(quantidade);
        }

        return recomendacoes.toList();
    }

    @Override
    public List<AnuncioDTO> buscarAleatorios(
            List<AnuncioDTO> anuncios,
            int quantidade
    ) {
        if (anuncios == null || anuncios.isEmpty()) {
            return List.of();
        }

        List<AnuncioDTO> anunciosAleatorios =
                new ArrayList<>(anuncios);

        Collections.shuffle(anunciosAleatorios);

        return anunciosAleatorios.stream()
                .limit(quantidade)
                .toList();
    }
}