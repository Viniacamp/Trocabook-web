package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.controllers.response.RecomendacaoResponse;

import java.util.List;

public interface IRecomendacaoCacheService {

    List<RecomendacaoResponse> buscarRanking(
            String uidUsuario
    );
}