package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.adapter.GoogleAPIBooksAdapter;
import com.trocabook.Trocabook.model.AutorFirebase;
import com.trocabook.Trocabook.model.CategoriaFirebase;
import com.trocabook.Trocabook.model.LivroFirebase;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;
import com.trocabook.Trocabook.repository.AutorFirebaseRepository;
import com.trocabook.Trocabook.repository.CategoriaFirebaseRepository;
import com.trocabook.Trocabook.repository.LivroFirebaseRepository;
import com.trocabook.Trocabook.service.GoogleAPIBooksService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.ITraducaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivroService implements ILivroService {

    private final LivroFirebaseRepository livroFirebaseRepository;
    private final AutorFirebaseRepository autorFirebaseRepository;
    private final CategoriaFirebaseRepository categoriaFirebaseRepository;
    private final GoogleAPIBooksService googleAPIBooksService;
    private final ITraducaoService traducaoService;
    private final GoogleAPIBooksAdapter adapter;

    public LivroService(
            LivroFirebaseRepository livroFirebaseRepository,
            AutorFirebaseRepository autorFirebaseRepository,
            CategoriaFirebaseRepository categoriaFirebaseRepository,
            GoogleAPIBooksService googleAPIBooksService,
            ITraducaoService traducaoService,
            GoogleAPIBooksAdapter adapter
    ) {
        this.livroFirebaseRepository = livroFirebaseRepository;
        this.autorFirebaseRepository = autorFirebaseRepository;
        this.categoriaFirebaseRepository = categoriaFirebaseRepository;
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
    public LivroFirebase cadastrar(LivroBuscaOutput livro) {

        /*
         * Verifica se o livro já foi cadastrado.
         */
        LivroFirebase livroExistente =
                livroFirebaseRepository.buscarPorGoogleBooksId(
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
                            autorFirebaseRepository.buscarPorNome(nome);

                    if (autorExistente != null) {
                        return autorExistente.getId();
                    }

                    AutorFirebase novoAutor =
                            new AutorFirebase();

                    novoAutor.setId(
                            java.util.UUID.randomUUID().toString()
                    );

                    novoAutor.setNome(nome);

                    autorFirebaseRepository.salvar(novoAutor);

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
                            categoriaFirebaseRepository.buscarPorNome(nome);

                    if (categoriaExistente != null) {
                        return categoriaExistente.getId();
                    }

                    CategoriaFirebase novaCategoria =
                            new CategoriaFirebase();

                    novaCategoria.setId(
                            java.util.UUID.randomUUID().toString()
                    );

                    novaCategoria.setNome(nome);

                    categoriaFirebaseRepository.salvar(novaCategoria);

                    return novaCategoria.getId();
                })
                .toList();

        /*
         * Converte o DTO de busca traduzido para a entidade Firebase.
         */
        LivroFirebase entidade =
                LivroFirebase.from(
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
        return livroFirebaseRepository.salvar(entidade);
    }

    @Override
    public LivroFirebase buscarPorUid(String uid) {
        return livroFirebaseRepository.buscarPorUid(uid);
    }

    @Override
    public LivroFirebase buscarPorGoogleBooksId(String googleBooksId) {
        return livroFirebaseRepository.buscarPorGoogleBooksId(googleBooksId);
    }

    @Override
    public List<LivroFirebase> buscarPorTitulo(String titulo) {
        return livroFirebaseRepository.buscarPorTitulo(titulo);
    }

    @Override
    public List<LivroFirebase> buscarTodos() {
        return livroFirebaseRepository.buscarTodos();
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