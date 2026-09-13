package com.trocabook.Trocabook.model.dto;
import java.util.List;

/**
 * DTO utilizado para representar a resposta retornada pela
 * <b>Google Books API</b>.
 *
 * <p>A estrutura contém os itens encontrados na consulta e suas
 * respectivas informações bibliográficas.</p>
 */
public record GoogleBooksResponse(List<Items> items) {


    /**
     * Representa um item retornado pela Google Books API.
     *
     * <p>Contém o identificador do livro e suas informações bibliográficas.</p>
     */
    public record Items(String id, VolumeInfo volumeInfo) {
        /**
         * Representa as informações bibliográficas de um livro retornadas pela API,
         * como título, autores, editora, data de publicação, descrição, imagem e idioma.
         */
        public record VolumeInfo(
                String title,

                List<String> authors,

                String publisher,

                String publishedDate,

                ImageLinks imageLinks,

                List<String> categories,

                String language) {


            /**
             * Representa os links das imagens associadas ao livro.
             */
            public record ImageLinks(String thumbnail) {
            }

        }
    }
}