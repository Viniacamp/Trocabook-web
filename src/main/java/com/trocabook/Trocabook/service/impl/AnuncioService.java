package com.trocabook.Trocabook.service.impl;

import com.google.cloud.Timestamp;
import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.Autor;
import com.trocabook.Trocabook.model.Categoria;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.AnuncioAnalyticsDTO;
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
import org.springframework.cache.annotation.CacheEvict;
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
    private final IaService iaService;

    public AnuncioService(
            IUsuarioService usuarioService,
            ILivroService livroService,
            AnuncioRepository anuncioRepository,
            AutorRepository autorRepository,
            CategoriaRepository categoriaRepository,
            CacheManager cacheManager,
            IaService iaService
    ) {
        this.usuarioService = usuarioService;
        this.livroService = livroService;
        this.anuncioRepository = anuncioRepository;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.cacheManager = cacheManager;
        this.iaService = iaService;
    }

    @Override
    @CacheEvict(
            value = "recomendacoes",
            allEntries = true
    )
    public AnuncioDTO anunciar(
            String uidLivro,
            String uidUsuario,
            String tipoNegociacao,
            String descricao
    ) {
        UsuarioOutput usuarioFirebase =
                usuarioService.buscarPorUid(uidUsuario);

        if (usuarioFirebase == null) {
            throw new IllegalArgumentException(
                    "Usuário não encontrado ao criar anúncio."
            );
        }

        Livro livro = livroService.buscarPorUid(uidLivro);

        if (livro == null) {
            throw new IllegalArgumentException(
                    "Livro não encontrado ao criar anúncio."
            );
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
                descricao,
                Anuncio.StatusAnuncio.ATIVO,
                Timestamp.now()
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
    public List<AnuncioDTO> listarAnunciosAtivosPorTipoNegociacao(
            Anuncio.TipoNegociacao tipoNegociacao
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache != null) {
            List<AnuncioDTO> anunciosCacheados =
                    buscarAnunciosNoCache(cache, "ativos");

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
                .listarTodosPorTipoNegociacao(tipoNegociacao, Anuncio.StatusAnuncio.ATIVO)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarAnunciosAtivosPorUsuario(
            String uidUsuario
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache != null) {
            List<AnuncioDTO> anunciosCacheados =
                    buscarAnunciosNoCache(cache, "ativos");

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
                .buscarPorUidUsuario(uidUsuario, Anuncio.StatusAnuncio.ATIVO)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarAnunciosAtivosPorUsuarioETipo(
            String uidUsuario,
            Anuncio.TipoNegociacao tipoNegociacao
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache != null) {
            List<AnuncioDTO> anunciosCacheados =
                    buscarAnunciosNoCache(cache, "ativos");

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
                        tipoNegociacao,
                        Anuncio.StatusAnuncio.ATIVO
                )
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> buscarAnunciosAtivosPorTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            return List.of();
        }

        String tituloNormalizado = titulo
                .trim()
                .toLowerCase(Locale.ROOT);

        return anuncioRepository
                .buscarPorTitulo(tituloNormalizado, Anuncio.StatusAnuncio.ATIVO)
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> buscarInteligenteAnunciosAtivos(String consulta) {
        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }

        String consultaNorm = iaService.normalizarTexto(consulta);
        List<AnuncioDTO> ativos = listarAtivos();

        if (ativos.isEmpty()) {
            return List.of();
        }

        boolean pareceFraseSemantica = consultaNorm.split("\\s+").length >= 3 || consultaNorm.contains("?");
        List<String> termosExpandidos = pareceFraseSemantica
                ? iaService.extrairTermosBuscaSemantica(consulta)
                : iaService.extrairTokensLocais(consulta);

        record AnuncioPontuado(AnuncioDTO anuncio, int pontos) {}

        List<AnuncioPontuado> ranqueados = ativos.stream()
                .map(anuncio -> {
                    int pontos = 0;
                    String tituloNorm = iaService.normalizarTexto(anuncio.titulo());
                    String descNorm = iaService.normalizarTexto(anuncio.descricao());
                    String autoresNorm = anuncio.autores() != null
                            ? iaService.normalizarTexto(String.join(" ", anuncio.autores()))
                            : "";
                    String categoriasNorm = anuncio.categorias() != null
                            ? iaService.normalizarTexto(String.join(" ", anuncio.categorias()))
                            : "";

                    if (tituloNorm.contains(consultaNorm)) pontos += 100;
                    if (autoresNorm.contains(consultaNorm)) pontos += 80;
                    if (categoriasNorm.contains(consultaNorm)) pontos += 60;
                    if (descNorm.contains(consultaNorm)) pontos += 40;

                    for (String termo : termosExpandidos) {
                        if (termo.length() < 2) continue;
                        if (tituloNorm.contains(termo)) pontos += 25;
                        if (autoresNorm.contains(termo)) pontos += 20;
                        if (categoriasNorm.contains(termo)) pontos += 15;
                        if (descNorm.contains(termo)) pontos += 10;
                    }

                    return new AnuncioPontuado(anuncio, pontos);
                })
                .filter(item -> item.pontos() > 0)
                .sorted((a, b) -> Integer.compare(b.pontos(), a.pontos()))
                .toList();

        if (ranqueados.isEmpty() && !pareceFraseSemantica) {
            List<String> termosIa = iaService.extrairTermosBuscaSemantica(consulta);
            ranqueados = ativos.stream()
                    .map(anuncio -> {
                        int pontos = 0;
                        String tudo = iaService.normalizarTexto(
                                anuncio.titulo() + " " +
                                        (anuncio.autores() != null ? String.join(" ", anuncio.autores()) : "") + " " +
                                        (anuncio.categorias() != null ? String.join(" ", anuncio.categorias()) : "") + " " +
                                        anuncio.descricao()
                        );
                        for (String termo : termosIa) {
                            if (termo.length() >= 2 && tudo.contains(termo)) {
                                pontos += 15;
                            }
                        }
                        return new AnuncioPontuado(anuncio, pontos);
                    })
                    .filter(item -> item.pontos() > 0)
                    .sorted((a, b) -> Integer.compare(b.pontos(), a.pontos()))
                    .toList();
        }

        return ranqueados.stream()
                .map(AnuncioPontuado::anuncio)
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
    @CacheEvict(
            value = "recomendacoes",
            allEntries = true
    )
    public AnuncioDTO finalizar(String uidAnuncio) {
        Anuncio anuncio =
                anuncioRepository.buscarPorUid(
                        uidAnuncio
                );

        if (anuncio == null) {
            throw new IllegalArgumentException(
                    "Anúncio não encontrado"
            );
        }

        if (anuncio.getStatus()
                == Anuncio.StatusAnuncio.FINALIZADO) {
            return converterParaDto(anuncio);
        }

        anuncio.setStatus(
                Anuncio.StatusAnuncio.FINALIZADO
        );

        anuncioRepository.atualizar(
                anuncio
        );

        AnuncioDTO anuncioAtualizado =
                converterParaDto(anuncio);

        atualizarNoCache(
                anuncioAtualizado
        );

        return anuncioAtualizado;
    }

    @Override
    @CacheEvict(
            value = "recomendacoes",
            allEntries = true
    )
    public void deletar(String uid) {
        anuncioRepository.deletar(uid);
        removerDoCache(uid);
    }

    @Override
    @Cacheable(
            value = "anuncios",
            key = "'ativos'"
    )
    public List<AnuncioDTO> listarAtivos() {
        return anuncioRepository
                .listarAtivos()
                .stream()
                .map(this::converterParaDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarAnunciosAtivosTrocaveis(
            String uidUsuario
    ) {
        if (uidUsuario == null || uidUsuario.isBlank()) {
            return List.of();
        }

        return listarAtivos()
                .stream()
                .filter(anuncio ->
                        uidUsuario.equals(
                                anuncio.uidUsuario()
                        )
                )
                .filter(anuncio ->
                        "TROCA".equals(
                                anuncio.tipoNegociacao()
                        )
                                || "AMBOS".equals(
                                anuncio.tipoNegociacao()
                        )
                )
                .toList();
    }

    @Override
    public List<AnuncioAnalyticsDTO> listarParaAnalise() {
        return anuncioRepository.listarTodos()
                .stream()
                .map(this::converterParaAnalytics)
                .toList();
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

    private AnuncioAnalyticsDTO converterParaAnalytics(Anuncio anuncio) {
        List<String> autores =
                buscarNomesAutores(
                        anuncio.getAutores()
                );

        List<String> categorias =
                buscarNomesCategorias(
                        anuncio.getCategorias()
                );

        return anuncio.paraAnalytics(
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

        adicionarAoCache(
                cache,
                "todos",
                novoAnuncio
        );

        if ("ATIVO".equals(novoAnuncio.status())) {
            adicionarAoCache(
                    cache,
                    "ativos",
                    novoAnuncio
            );
        }
    }

    private void atualizarNoCache(
            AnuncioDTO anuncioAtualizado
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache == null) {
            return;
        }

        atualizarNoCache(
                cache,
                "todos",
                anuncioAtualizado
        );

        if ("FINALIZADO".equals(anuncioAtualizado.status())) {
            removerDoCache(
                    cache,
                    "ativos",
                    anuncioAtualizado.id()
            );
        } else {
            atualizarNoCache(
                    cache,
                    "ativos",
                    anuncioAtualizado
            );
        }
    }

    private void removerDoCache(
            String uidAnuncio
    ) {
        Cache cache =
                cacheManager.getCache("anuncios");

        if (cache == null) {
            return;
        }

        removerDoCache(
                cache,
                "todos",
                uidAnuncio
        );

        removerDoCache(
                cache,
                "ativos",
                uidAnuncio
        );
    }

    private void adicionarAoCache(
            Cache cache,
            String chave,
            AnuncioDTO novoAnuncio
    ) {
        List<AnuncioDTO> anuncios =
                buscarAnunciosNoCache(
                        cache,
                        chave
                );

        if (anuncios == null) {
            return;
        }

        List<AnuncioDTO> atualizados =
                new ArrayList<>(anuncios);

        atualizados.add(novoAnuncio);

        cache.put(
                chave,
                List.copyOf(atualizados)
        );
    }

    private void atualizarNoCache(
            Cache cache,
            String chave,
            AnuncioDTO anuncioAtualizado
    ) {
        List<AnuncioDTO> anuncios =
                buscarAnunciosNoCache(
                        cache,
                        chave
                );

        if (anuncios == null) {
            return;
        }

        List<AnuncioDTO> atualizados =
                anuncios.stream()
                        .map(anuncio ->
                                anuncio.id().equals(
                                        anuncioAtualizado.id()
                                )
                                        ? anuncioAtualizado
                                        : anuncio
                        )
                        .toList();

        cache.put(
                chave,
                atualizados
        );
    }

    private void removerDoCache(
            Cache cache,
            String chave,
            String uidAnuncio
    ) {
        List<AnuncioDTO> anuncios =
                buscarAnunciosNoCache(
                        cache,
                        chave
                );

        if (anuncios == null) {
            return;
        }

        List<AnuncioDTO> atualizados =
                anuncios.stream()
                        .filter(anuncio ->
                                !anuncio.id().equals(uidAnuncio)
                        )
                        .toList();

        cache.put(
                chave,
                atualizados
        );
    }

    @SuppressWarnings("unchecked")
    private List<AnuncioDTO> buscarAnunciosNoCache(
            Cache cache,
            String chave
    ) {
        return cache.get(
                chave,
                List.class
        );
    }
}