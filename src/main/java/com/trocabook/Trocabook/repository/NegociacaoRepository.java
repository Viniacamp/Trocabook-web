package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Negociacao;

import java.util.List;

public interface NegociacaoRepository {

    Negociacao salvar(Negociacao negociacao);

    Negociacao buscarPorUid(String uid);

    List<Negociacao> buscarPorUidUsuarioAnunciante(String uidAnunciante);

    List<Negociacao> buscarPorUidUsuarioComprador(String uidComprador);

    List<Negociacao> buscarPorUidAnuncianteETipoNegociacao(String uidAnunciante, Negociacao.TipoNegociacao tipoNegociacao);

    List<Negociacao> buscarPorUidCompradorETipoNegociacao(String uidComprador, Negociacao.TipoNegociacao tipoNegociacao);

    long contarNegociacoesPorUsuarioETipo(String uidAnunciante, Negociacao.TipoNegociacao tipoNegociacao);
}
