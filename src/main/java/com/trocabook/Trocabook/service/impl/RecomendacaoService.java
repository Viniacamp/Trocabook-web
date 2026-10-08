package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.controllers.response.RecomendacaoResponse;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.service.IRecomendacaoCacheService;
import com.trocabook.Trocabook.service.IRecomendacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecomendacaoService implements IRecomendacaoService {

    private final IRecomendacaoCacheService recomendacaoCacheService;

    private static final Logger logger =
            LoggerFactory.getLogger(RecomendacaoService.class);

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

        List<AnuncioDTO> anunciosAtivos =
                anuncios.stream()
                        .filter(anuncio ->
                                "ATIVO".equals(anuncio.status())
                        )
                        .toList();

        if (anunciosAtivos.isEmpty()) {
            return List.of();
        }


        List<RecomendacaoResponse> ranking;

        try {
            ranking =
                    recomendacaoCacheService.buscarRanking(
                            uidUsuario
                    );
        } catch (Exception ex) {

            logger.warn(
                    "Serviço de recomendação indisponível. " +
                            "Utilizando anúncios aleatórios para o usuário {}.",
                    uidUsuario
            );

            logger.debug(
                    "Erro ao consultar serviço de recomendação.",
                    ex
            );


            return buscarAleatorios(
                    anunciosAtivos,
                    quantidade > 0
                            ? quantidade
                            : anunciosAtivos.size()
            );
        }

        if (ranking == null || ranking.isEmpty()) {
            return buscarAleatorios(
                    anunciosAtivos,
                    quantidade > 0
                            ? quantidade
                            : anunciosAtivos.size()
            );
        }

        Map<String, AnuncioDTO> anunciosPorId =
                anunciosAtivos.stream()
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
    public List<AnuncioDTO> ordenarPorRecomendacao(
            String uidUsuario,
            List<AnuncioDTO> anuncios
    ) {
        if (uidUsuario == null
                || uidUsuario.isBlank()
                || anuncios == null
                || anuncios.isEmpty()) {

            return anuncios == null
                    ? List.of()
                    : anuncios;
        }

        List<RecomendacaoResponse> ranking;

        try {
            ranking =
                    recomendacaoCacheService.buscarRanking(
                            uidUsuario
                    );
        } catch (Exception ex) {
            logger.warn(
                    "Serviço de recomendação indisponível. " +
                            "Mantendo ordenação original dos anúncios " +
                            "para o usuário {}.",
                    uidUsuario
            );

            logger.debug(
                    "Erro ao consultar serviço de recomendação.",
                    ex
            );

            return anuncios;
        }

        if (ranking == null || ranking.isEmpty()) {
            return anuncios;
        }

        Map<String, Integer> posicaoRanking =
                new HashMap<>();

        for (int i = 0; i < ranking.size(); i++) {
            posicaoRanking.put(
                    ranking.get(i).uidAnuncio(),
                    i
            );
        }

        return anuncios.stream()
                .sorted(
                        Comparator.comparingInt(
                                anuncio ->
                                        posicaoRanking.getOrDefault(
                                                anuncio.id(),
                                                Integer.MAX_VALUE
                                        )
                        )
                )
                .toList();
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