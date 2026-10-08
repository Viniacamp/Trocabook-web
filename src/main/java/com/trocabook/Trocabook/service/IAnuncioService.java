package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;

import java.util.List;

public interface IAnuncioService {
    AnuncioDTO anunciar(String uidLivro, String uidUsuario, String tipoNegociacao, String descricao);


    AnuncioDTO buscarPorUid(String uid);

    List<AnuncioDTO> listarTodos();

    List<AnuncioDTO> listarAnunciosAtivosPorTipoNegociacao(Anuncio.TipoNegociacao tipoNegociacao);

    List<AnuncioDTO> listarAnunciosAtivosPorUsuario(String uidUsuario);

    List<AnuncioDTO> listarAnunciosAtivosPorUsuarioETipo(String uidUsuario, Anuncio.TipoNegociacao tipoNegociacao);

    List<AnuncioDTO> buscarAnunciosAtivosPorTitulo(String titulo);

    AnuncioDTO atualizar(
            String uidAnuncio,
            String uidUsuario,
            String descricao,
            String tipoNegociacao
    );

    List<AnuncioDTO> listarAtivos();

    List<AnuncioDTO> listarAnunciosAtivosTrocaveis(
            String uidUsuario
    );

    AnuncioDTO finalizar(String uidAnuncio);

    void deletar(String uid);



}
