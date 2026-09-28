package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.dto.InteracaoDTO;

import java.util.List;

public interface IInteracaoService {

    InteracaoDTO registrarPesquisa(
            String uidUsuario,
            String termoPesquisa
    );

    InteracaoDTO registrarVisualizacao(
            String uidUsuario,
            String uidLivro,
            String uidAnuncio
    );

    InteracaoDTO registrarInicioConversa(
            String uidUsuario,
            String uidLivro,
            String uidAnuncio
    );

    List<InteracaoDTO> listarTodos();

    List<InteracaoDTO> listarPorUsuario(String uidUsuario);
}