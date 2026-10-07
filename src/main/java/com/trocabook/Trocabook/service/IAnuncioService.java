package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;

import java.util.List;

public interface IAnuncioService {
    AnuncioDTO anunciar(String uidLivro, String uidUsuario, String tipoNegociacao, String descricao);


    AnuncioDTO buscarPorUid(String uid);

    List<AnuncioDTO> listarTodos();

    List<AnuncioDTO> listarTodosPorTipoNegociacao(Anuncio.TipoNegociacao tipoNegociacao);

    List<AnuncioDTO> listarAnunciosUsuario(String uidUsuario);

    List<AnuncioDTO> listarAnunciosUsuarioETipo(String uidUsuario, Anuncio.TipoNegociacao tipoNegociacao);

    List<AnuncioDTO> buscarPorTitulo(String titulo);

    AnuncioDTO atualizar(
            String uidAnuncio,
            String uidUsuario,
            String descricao,
            String tipoNegociacao
    );

    void deletar(String uid);



}
