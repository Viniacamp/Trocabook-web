package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Negociacao;

import java.util.Arrays;
import java.util.List;

public interface NegociacaoRepository {

    Negociacao salvar(Negociacao negociacao);

    Negociacao buscarPorUid(String uid);

    List<Negociacao> buscarPorUidUsuarioAnunciante(String uidAnunciante);

    List<Negociacao> buscarPorUidUsuarioComprador(String uidComprador);

    List<Negociacao> buscarPorUidAnuncianteETipoNegociacao(String uidAnunciante, Negociacao.TipoNegociacao tipoNegociacao);

    List<Negociacao> buscarPorUidCompradorETipoNegociacao(String uidComprador, Negociacao.TipoNegociacao tipoNegociacao);

    List<Negociacao> buscarPorAnuncioId(String anuncioId);

    long contarNegociacoesPorUsuarioETipo(String uidAnunciante, Negociacao.TipoNegociacao tipoNegociacao);

    Negociacao buscarPorAnuncioEComprador(
            String anuncioId,
            String usuarioCompradorId
    );

    List<Negociacao> buscarPorAnuncioOferecidoId(String anuncioOferecidoId);

    List<Negociacao> buscarFinalizadasPorUsuario(
            String uidUsuario
    );

    List<Negociacao> buscarFinalizadasPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao
    );
}
