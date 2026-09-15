package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.adapter.GoogleAPIBooksAdapter;
import com.trocabook.Trocabook.model.Autor;
import com.trocabook.Trocabook.model.Categoria;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;
import com.trocabook.Trocabook.repository.AutorRepository;
import com.trocabook.Trocabook.repository.CategoriaRepository;
import com.trocabook.Trocabook.repository.LivroRepository;
import com.trocabook.Trocabook.service.feign.GoogleAPIBooksService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.ITraducaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivroService implements ILivroService {

    private final LivroRepository livroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final GoogleAPIBooksService googleAPIBooksService;
    private final ITraducaoService traducaoService;
    private final GoogleAPIBooksAdapter adapter;

    public LivroService(
            LivroRepository livroRepository,
            AutorRepository autorRepository,
            CategoriaRepository categoriaRepository,
            GoogleAPIBooksService googleAPIBooksService,
            ITraducaoService traducaoService,
            GoogleAPIBooksAdapter adapter
    ) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.googleAPIBooksService = googleAPIBooksService;
        this.traducaoService = traducaoService;
        this.adapter = adapter;
    }

    @Override
    public List<LivroBuscaOutput> pesquisarLivros(String titulo) {
        return adapter.adaptar(
                googleAPIBooksService.buscarTitulo(titulo)
        );
    }

    @Override
    public Livro cadastrar(LivroBuscaOutput livro) {

        /*
         * Verifica se o livro já foi cadastrado.
         */
        Livro livroExistente =
                livroRepository.buscarPorGoogleBooksId(
                        livro.googleBooksId()
                );

        if (livroExistente != null) {
            return livroExistente;
        }

        /*
         * Traduz o título e as categorias em uma única chamada
         * para a MyMemory API.
         */
        LivroBuscaOutput livroTraduzido = traduzirLivro(livro);

        /*
         * Obtém os IDs dos autores.
         */
        List<String> idsAutores = livroTraduzido.autores()
                .stream()
                .map(nome -> {

                    var autorExistente =
                            autorRepository.buscarPorNome(nome);

                    if (autorExistente != null) {
                        return autorExistente.getId();
                    }

                    Autor novoAutor =
                            new Autor();

                    novoAutor.setId(
                            java.util.UUID.randomUUID().toString()
                    );

                    novoAutor.setNome(nome);

                    autorRepository.salvar(novoAutor);

                    return novoAutor.getId();
                })
                .toList();

        /*
         * Obtém os IDs das categorias.
         */
        List<String> idsCategorias = livroTraduzido.categorias()
                .stream()
                .map(nome -> {

                    var categoriaExistente =
                            categoriaRepository.buscarPorNome(nome);

                    if (categoriaExistente != null) {
                        return categoriaExistente.getId();
                    }

                    Categoria novaCategoria =
                            new Categoria();

                    novaCategoria.setId(
                            java.util.UUID.randomUUID().toString()
                    );

                    novaCategoria.setNome(nome);

                    categoriaRepository.salvar(novaCategoria);

                    return novaCategoria.getId();
                })
                .toList();

        /*
         * Converte o DTO de busca traduzido para a entidade Firebase.
         */
        Livro entidade =
                Livro.from(
                        livroTraduzido,
                        idsAutores,
                        idsCategorias
                );

        /*
         * Gera o ID interno do livro.
         */
        entidade.setId(
                java.util.UUID.randomUUID().toString()
        );

        /*
         * Persiste o livro.
         */
        return livroRepository.salvar(entidade);
    }

    @Override
    public Livro buscarPorUid(String uid) {
        return livroRepository.buscarPorUid(uid);
    }

    @Override
    public Livro buscarPorGoogleBooksId(String googleBooksId) {
        return livroRepository.buscarPorGoogleBooksId(googleBooksId);
    }

    @Override
    public List<Livro> buscarPorTitulo(String titulo) {
        return livroRepository.buscarPorTitulo(titulo);
    }

    @Override
    public List<Livro> buscarTodos() {
        return livroRepository.buscarTodos();
    }

    private LivroBuscaOutput traduzirLivro(LivroBuscaOutput livro) {

        /*
         * Se o livro já estiver em português, não há necessidade
         * de realizar a chamada à API.
         */
        if ("pt".equalsIgnoreCase(livro.lingua())
                || "pt-BR".equalsIgnoreCase(livro.lingua())) {

            return livro;
        }


        String traducao =
                traducaoService.traduzirTituloECategorias(
                        livro.titulo(),
                        livro.categorias(),
                        livro.lingua()
                );

        String[] partes = traducao.split(
                "\\s*\\|\\|\\|\\s*",
                2
        );

        String tituloTraduzido = partes[0].trim();

        List<String> categoriasTraduzidas;

        if (partes.length > 1 && !partes[1].isBlank()) {

            categoriasTraduzidas =
                    List.of(
                                    partes[1]
                                            .split("\\s*###\\s*")
                            )
                            .stream()
                            .map(String::trim)
                            .filter(categoria -> !categoria.isBlank())
                            .toList();

        } else {
            categoriasTraduzidas = List.of();
        }

        return new LivroBuscaOutput(
                livro.googleBooksId(),
                tituloTraduzido,
                livro.autores(),
                livro.publicadora(),
                livro.dataPublicacao(),
                livro.urlImagem(),
                livro.lingua(),
                categoriasTraduzidas
        );
    }


}