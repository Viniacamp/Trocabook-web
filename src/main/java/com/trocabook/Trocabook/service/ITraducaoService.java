package com.trocabook.Trocabook.service;

import java.util.List;

/**
 * Interface que define o serviço de <b>Strategy</b> responsável pelas
 * operações de tradução dos dados dos livros.
 *
 * <p>Permite abstrair a implementação utilizada para realizar as traduções,
 * mantendo o restante da aplicação independente da API de tradução.</p>
 *
 * @author PedroAmaralMa
 */
public interface ITraducaoService {

    /**
     * Traduz o título de um livro a partir do idioma original informado.
     *
     * @param titulo título que será traduzido
     * @param linguaOriginal idioma original do título
     * @return título traduzido
     */
    String traduzirTituloECategorias(String titulo, List<String> categorias, String linguaOriginal);

}
