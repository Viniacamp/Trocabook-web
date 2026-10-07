package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.Autor;
import com.trocabook.Trocabook.model.Categoria;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.repository.AnuncioRepository;
import com.trocabook.Trocabook.repository.AutorRepository;
import com.trocabook.Trocabook.repository.CategoriaRepository;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.IUsuarioService;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class AnuncioService implements IAnuncioService {

    private final IUsuarioService usuarioService;
    private final ILivroService livroService;
    private final AnuncioRepository anuncioRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final CacheManager cacheManager;

    public AnuncioService(
            IUsuarioService usuarioService,
            ILivroService livroService,
            AnuncioRepository anuncioRepository,
            AutorRepository autorRepository,
            CategoriaRepository categoriaRepository,
            CacheManager cacheManager
    ) {
        this.usuarioService = usuarioService;
        this.livroService = livroService;
        this.anuncioRepository = anuncioRepository;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public AnuncioDTO anunciar(
            String uidLivro,
            String uidUsuario,
            String tipoNegociacao,
            String descricao
    ) {
        UsuarioOutput usuarioFirebase =
                usuarioService.buscarPorUid(uidUsuario);

        if (usuarioFirebase == null) {
            return null;
        }

        Livro livro = livroService.buscarPorUid(uidLivro);

        if (livro == null) {
            return null;
        }

        if (descricao == null || descricao.isBlank()) {
            descricao = "Anunciante não informou uma descrição.";
        }

        Anuncio anuncio = new Anuncio(
                UUID.randomUUID().toString(),
                uidUsuario,
                uidLivro,
                usuarioFirebase.nome(),
                Anuncio.TipoNegociacao.valueOf(tipoNegociacao),
                livro.getTitulo(),
                usuarioFirebase.fotoPerfil(),
                livro.getUrlImagem(),
                livro.getIdsAutores(),
                livro.getIdsCategorias(),
                descricao
        );

        anuncioRepository.salvar(anuncio);

        AnuncioDTO anuncioDTO =
                converterParaDto(anuncio);

        adicionarAoCache(anuncioDTO);

        return anuncioDTO;
    }

    @Override
    public AnuncioDTO buscarPorUid(String uid) {
        Anuncio anuncio =
                anuncioRepository.buscarPorUid(uid);

        if (anuncio == null) {
            return null;
        }

        return converterParaDto(anuncio);
    }

    @Override
    @Cacheable(
            value = "anuncios",
            key = "'todos'"
    )
    public List<AnuncioDTO> listarTodos() {
        return anuncioRepository
                .listarTodos()
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarTodosPorTipoNegociacao(
            Anuncio.TipoNegociacao tipoNegociacao
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache != null) {
            List<AnuncioDTO> anunciosCacheados =
                    buscarAnunciosNoCache(cache);

            if (anunciosCacheados != null) {
                return anunciosCacheados.stream()
                        .filter(anuncio ->
                                tipoNegociacao.name().equals(
                                        anuncio.tipoNegociacao()
                                )
                        )
                        .toList();
            }
        }

        return anuncioRepository
                .listarTodosPorTipoNegociacao(tipoNegociacao)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }


    @Override
    public List<AnuncioDTO> listarAnunciosUsuario(
            String uidUsuario
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache != null) {
            List<AnuncioDTO> anunciosCacheados =
                    buscarAnunciosNoCache(cache);

            if (anunciosCacheados != null) {
                return anunciosCacheados.stream()
                        .filter(anuncio ->
                                uidUsuario.equals(
                                        anuncio.uidUsuario()
                                )
                        )
                        .toList();
            }
        }

        return anuncioRepository
                .buscarPorUidUsuario(uidUsuario)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarAnunciosUsuarioETipo(
            String uidUsuario,
            Anuncio.TipoNegociacao tipoNegociacao
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache != null) {
            List<AnuncioDTO> anunciosCacheados =
                    buscarAnunciosNoCache(cache);

            if (anunciosCacheados != null) {
                return anunciosCacheados.stream()
                        .filter(anuncio ->
                                uidUsuario.equals(
                                        anuncio.uidUsuario()
                                )
                        )
                        .filter(anuncio ->
                                tipoNegociacao.name().equals(
                                        anuncio.tipoNegociacao()
                                )
                        )
                        .toList();
            }
        }

        return anuncioRepository
                .buscarPorUidUsuarioETipoNegociacao(
                        uidUsuario,
                        tipoNegociacao
                )
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> buscarPorTitulo(String titulo) {

        if (titulo == null || titulo.isBlank()) {
            return List.of();
        }

        String tituloNormalizado = titulo
                .trim()
                .toLowerCase(Locale.ROOT);

        return anuncioRepository
                .buscarPorTitulo(tituloNormalizado)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public AnuncioDTO atualizar(
            String uidAnuncio,
            String uidUsuario,
            String descricao,
            String tipoNegociacao
    ) {
        Anuncio anuncio =
                anuncioRepository.buscarPorUid(uidAnuncio);

        if (anuncio == null) {
            return null;
        }

        if (!anuncio.getUidUsuario().equals(uidUsuario)) {
            throw new IllegalArgumentException(
                    "Usuário não possui permissão para editar este anúncio."
            );
        }

        if (descricao == null || descricao.isBlank()) {
            descricao = "Anunciante não informou uma descrição.";
        }

        Anuncio.TipoNegociacao tipo;

        try {
            tipo = Anuncio.TipoNegociacao.valueOf(
                    tipoNegociacao.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Tipo de negociação inválido."
            );
        }

        anuncio.setDescricao(descricao);
        anuncio.setTipoNegociacao(tipo);

        anuncioRepository.atualizar(anuncio);

        AnuncioDTO anuncioAtualizado =
                converterParaDto(anuncio);

        atualizarNoCache(anuncioAtualizado);

        return anuncioAtualizado;
    }

    @Override
    public void deletar(String uid) {
        anuncioRepository.deletar(uid);

        removerDoCache(uid);
    }

    private List<String> buscarNomesAutores(
            List<String> idsAutores
    ) {
        if (idsAutores == null || idsAutores.isEmpty()) {
            return List.of();
        }

        return idsAutores.stream()
                .map(autorRepository::buscarPorUid)
                .filter(Objects::nonNull)
                .map(Autor::getNome)
                .toList();
    }

    private List<String> buscarNomesCategorias(
            List<String> idsCategorias
    ) {
        if (idsCategorias == null || idsCategorias.isEmpty()) {
            return List.of();
        }

        return idsCategorias.stream()
                .map(categoriaRepository::buscarPorUid)
                .filter(Objects::nonNull)
                .map(Categoria::getNome)
                .toList();
    }

    private AnuncioDTO converterParaDto(
            Anuncio anuncio
    ) {
        List<String> autores =
                buscarNomesAutores(
                        anuncio.getAutores()
                );

        List<String> categorias =
                buscarNomesCategorias(
                        anuncio.getCategorias()
                );

        return anuncio.paraDto(
                autores,
                categorias
        );
    }

    private void adicionarAoCache(
            AnuncioDTO novoAnuncio
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache == null) {
            return;
        }

        List<AnuncioDTO> anunciosCacheados =
                buscarAnunciosNoCache(cache);

        if (anunciosCacheados == null) {
            return;
        }

        List<AnuncioDTO> anunciosAtualizados =
                new ArrayList<>(anunciosCacheados);

        anunciosAtualizados.add(novoAnuncio);

        cache.put(
                "todos",
                List.copyOf(anunciosAtualizados)
        );
    }

    private void atualizarNoCache(
            AnuncioDTO anuncioAtualizado
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache == null) {
            return;
        }

        List<AnuncioDTO> anunciosCacheados =
                buscarAnunciosNoCache(cache);

        if (anunciosCacheados == null) {
            return;
        }

        List<AnuncioDTO> anunciosAtualizados =
                anunciosCacheados.stream()
                        .map(anuncio ->
                                anuncio.id().equals(
                                        anuncioAtualizado.id()
                                )
                                        ? anuncioAtualizado
                                        : anuncio
                        )
                        .toList();

        cache.put(
                "todos",
                anunciosAtualizados
        );
    }

    private void removerDoCache(
            String uidAnuncio
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache == null) {
            return;
        }

        List<AnuncioDTO> anunciosCacheados =
                buscarAnunciosNoCache(cache);

        if (anunciosCacheados == null) {
            return;
        }

        List<AnuncioDTO> anunciosAtualizados =
                anunciosCacheados.stream()
                        .filter(anuncio ->
                                !anuncio.id().equals(uidAnuncio)
                        )
                        .toList();

        cache.put(
                "todos",
                anunciosAtualizados
        );
    }

    @SuppressWarnings("unchecked")
    private List<AnuncioDTO> buscarAnunciosNoCache(
            Cache cache
    ) {
        return cache.get(
                "todos",
                List.class
        );
    }
}