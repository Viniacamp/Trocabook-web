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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
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
    private final CacheManager cacheManager;

    public LivroService(
            LivroRepository livroRepository,
            AutorRepository autorRepository,
            CategoriaRepository categoriaRepository,
            GoogleAPIBooksService googleAPIBooksService,
            ITraducaoService traducaoService,
            GoogleAPIBooksAdapter adapter,
            CacheManager cacheManager
    ) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.googleAPIBooksService = googleAPIBooksService;
        this.traducaoService = traducaoService;
        this.adapter = adapter;
        this.cacheManager = cacheManager;
    }

    @Override
    public List<LivroBuscaOutput> pesquisarLivros(String titulo) {
        return adapter.adaptar(
                googleAPIBooksService.buscarTitulo(titulo)
        );
    }

    @Override
    public Livro cadastrar(LivroBuscaOutput livro) {
        if (livro == null) {
            throw new IllegalArgumentException(
                    "Os dados do livro são obrigatórios."
            );
        }

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


        List<String> autores =
                validarEFiltrarLista(
                        livro.autores(),
                        "O livro precisa ter pelo menos um autor."
                );

        List<String> categorias =
                validarEFiltrarLista(
                        livro.categorias(),
                        "O livro precisa ter pelo menos uma categoria."
                );

        LivroBuscaOutput livroValidado =
                new LivroBuscaOutput(
                        livro.googleBooksId(),
                        livro.titulo(),
                        autores,
                        livro.publicadora(),
                        livro.dataPublicacao(),
                        livro.urlImagem(),
                        livro.lingua(),
                        categorias
                );

        /*
         * Traduz o título e as categorias em uma única chamada
         * para a MyMemory API.
         */
        LivroBuscaOutput livroTraduzido = traduzirLivro(livroValidado);

        /*
         * Obtém os IDs dos autores.
         */
        List<String> idsAutores = obterIdsAutores(livroTraduzido.autores());

        /*
         * Obtém os IDs das categorias.
         */
        List<String> idsCategorias = obterIdsCategorias(livroTraduzido.categorias());

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

    @Override
    public Livro cadastrarManual(
            String titulo,
            List<String> autores,
            List<String> categorias,
            String urlImagem
    ) {

        List<String> autoresValidos =
                validarEFiltrarLista(
                        autores,
                        "O livro precisa ter pelo menos um autor."
                );

        List<String> categoriasValidas =
                validarEFiltrarLista(
                        categorias,
                        "O livro precisa ter pelo menos uma categoria."
                );

        List<String> idsAutores =
                obterIdsAutores(autoresValidos);

        List<String> idsCategorias =
                obterIdsCategorias(categoriasValidas);


        Livro livro = new Livro();

        livro.setId(
                java.util.UUID.randomUUID().toString()
        );

        livro.setGoogleBooksId(null);
        livro.setTitulo(titulo);
        livro.setIdsAutores(idsAutores);
        livro.setIdsCategorias(idsCategorias);
        livro.setPublicadora(null);
        livro.setDataPublicacao(null);
        livro.setUrlImagem(urlImagem);

        return livroRepository.salvar(livro);
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

    @Override
    @Cacheable(value = "categorias", key = "'todas'")
    public List<Categoria> listarCategorias() {
        return categoriaRepository.buscarTodos();
    }

    private List<String> obterIdsAutores(List<String> autores) {

        return autores.stream()
                .map(String::trim)
                .filter(nome -> !nome.isBlank())
                .distinct()
                .map(nome -> {

                    Autor autorExistente =
                            autorRepository.buscarPorNome(nome);

                    if (autorExistente != null) {
                        return autorExistente.getId();
                    }

                    Autor novoAutor = new Autor();

                    novoAutor.setId(
                            java.util.UUID.randomUUID().toString()
                    );

                    novoAutor.setNome(nome);

                    autorRepository.salvar(novoAutor);

                    return novoAutor.getId();
                })
                .toList();
    }

    private List<String> obterIdsCategorias(List<String> categorias) {

        return categorias.stream()
                .map(String::trim)
                .filter(nome -> !nome.isBlank())
                .distinct()
                .map(nome -> {

                    Categoria categoriaExistente =
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

                    Cache cache = cacheManager.getCache("categorias");

                    if (cache != null) {
                        cache.evict("todas");
                    }

                    return novaCategoria.getId();
                })
                .toList();
    }

    private List<String> validarEFiltrarLista(
            List<String> valores,
            String mensagemErro
    ) {

        if (valores == null) {
            throw new IllegalArgumentException(mensagemErro);
        }

        List<String> valoresValidos =
                valores.stream()
                        .filter(valor -> valor != null && !valor.isBlank())
                        .map(String::trim)
                        .distinct()
                        .toList();

        if (valoresValidos.isEmpty()) {
            throw new IllegalArgumentException(mensagemErro);
        }

        return valoresValidos;
    }


}