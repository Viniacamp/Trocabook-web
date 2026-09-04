package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.dto.AnuncioDTO;

import java.util.List;

public interface IAnuncioService {
    AnuncioDTO anunciar(String uidLivro, String uidUsuario, String tipoNegociacao);

    List<AnuncioDTO> listarAnunciosUsuario(String uidUsuario);

    AnuncioDTO atualizar(AnuncioDTO anuncioDTO);

    void deletar(String uid);



}
