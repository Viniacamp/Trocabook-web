package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.model.dto.NegociacaoDTO;

import java.util.List;

public interface INegociacaoService {

    NegociacaoDTO criar(
            String anuncioId,
            String uidInteressado,
            Negociacao.TipoNegociacao tipoNegociacao
    );

    NegociacaoDTO buscarPorAnuncioEComprador(
            String anuncioId,
            String usuarioCompradorId
    );

    NegociacaoDTO aceitar(
            String uidNegociacao,
            String uidUsuario
    );

    NegociacaoDTO recusar(
            String uidNegociacao,
            String uidUsuario
    );



    NegociacaoDTO salvar(NegociacaoDTO negociacaoDTO);

    NegociacaoDTO buscarPorUid(String uid);

    List<NegociacaoDTO> listarPorUsuarioAnunciante(String uidAnunciante);

    List<NegociacaoDTO> listarPorUsuarioComprador(String uidComprador);

    List<NegociacaoDTO> listarPorUsuarioAnuncianteETipo(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao
    );

    List<NegociacaoDTO> listarPorUsuarioCompradorETipo(
            String uidComprador,
            Negociacao.TipoNegociacao tipoNegociacao
    );

    long contarNegociacoesPorUsuarioETipo(String uidAnunciante, Negociacao.TipoNegociacao tipoNegociacao);
}