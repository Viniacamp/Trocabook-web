package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.dto.AnuncioDTO;

import java.util.List;

public interface IRecomendacaoService {



    List<AnuncioDTO> buscarRecomendacoes(
            String uidUsuario,
            int quantidade,
            List<AnuncioDTO> anuncios
    );

    List<AnuncioDTO> ordenarPorRecomendacao(
            String uidUsuario,
            List<AnuncioDTO> anuncios
    );

    List<AnuncioDTO> buscarAleatorios(
            List<AnuncioDTO> anuncios,
            int quantidade
    );


}