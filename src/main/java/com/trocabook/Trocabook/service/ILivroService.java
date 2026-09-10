package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;

import java.util.List;

public interface ILivroService {

    /**
     * Pesquisa livros pelo título utilizando a Google Books API.
     *
     * @param titulo título informado pelo usuário
     * @return lista de livros encontrados para exibição na busca
     */
    List<LivroBuscaOutput> pesquisarLivros(String titulo);

    /**
     * Cadastra um livro no catálogo do Trocabook.
     *
     * @param livro dados do livro selecionado pelo usuário
     * @return livro persistido no Firebase
     */
    Livro cadastrar(LivroBuscaOutput livro);

    /**
     * Busca um livro pelo UID interno do Firebase.
     *
     * @param uid identificador do livro
     * @return livro encontrado ou null caso não exista
     */
    Livro buscarPorUid(String uid);

    /**
     * Busca um livro pelo identificador da Google Books.
     *
     * @param googleBooksId identificador externo da Google Books API
     * @return livro encontrado ou null caso não exista
     */
    Livro buscarPorGoogleBooksId(String googleBooksId);

    /**
     * Busca livros já cadastrados no catálogo pelo título.
     *
     * @param titulo título do livro
     * @return lista de livros encontrados
     */
    List<Livro> buscarPorTitulo(String titulo);

    /**
     * Retorna todos os livros cadastrados no catálogo.
     *
     * @return lista de livros
     */
    List<Livro> buscarTodos();
}