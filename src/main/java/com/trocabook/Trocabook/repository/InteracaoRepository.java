package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Interacao;

import java.util.List;

public interface InteracaoRepository {

    Interacao salvar(Interacao interacao);

    List<Interacao> listarTodos();

    List<Interacao> buscarPorUidUsuario(String uidUsuario);

    List<Interacao> buscarPorUidUsuarioETipo(
            String uidUsuario,
            Interacao.TipoInteracao tipoInteracao
    );

    List<Interacao> buscarPorUidUsuarioEAnuncioETipo(
            String uidUsuario,
            String uidAnuncio,
            Interacao.TipoInteracao tipoInteracao
    );
}