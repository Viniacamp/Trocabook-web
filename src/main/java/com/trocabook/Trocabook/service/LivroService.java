package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.*;
import com.trocabook.Trocabook.model.dto.GoogleBooksResponse;
import com.trocabook.Trocabook.model.dto.LivroDTO;
import com.trocabook.Trocabook.repository.*;
import com.trocabook.Trocabook.utils.TextoUtils;
// import jakarta.transaction.Transactional; // REMOVIDO
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // MANTIDO
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/*
@Service
public class LivroService {

    // --- Logger (Boa Prática) ---
    private static final Logger logger = LoggerFactory.getLogger(LivroService.class);

    // --- Constante (Boa Prática) ---
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    // --- Repositórios e Serviços ---
    private final LivroRepository livroRepository;
    private final UsuarioLivroRepository usuarioLivroRepository;
    private final FileStorageServiceUsuario fileStorageService;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final LivroAutorRepository livroAutorRepository;
    private final LivroCategoriaRepository livroCategoriaRepository;

    private final TraducaoService traducaoService;

    private final String url = "https://www.googleapis.com/books/v1/volumes?q=";

    private final RestTemplate restTemplate;

    @Autowired // Opcional, mas boa prática
    public LivroService(LivroRepository livroRepository, UsuarioLivroRepository usuarioLivroRepository,
                        FileStorageServiceUsuario fileStorageService,
                        RestTemplate restTemplate,
                        AutorRepository autorRepository,
                        LivroCategoriaRepository livroCategoriaRepository,
                        CategoriaRepository categoriaRepository,
                        LivroAutorRepository livroAutorRepository,
                        TraducaoService traducaoService) {
        // Construtor corrigido
        this.livroRepository = livroRepository;
        this.usuarioLivroRepository = usuarioLivroRepository;
        this.fileStorageService = fileStorageService;
        this.restTemplate = restTemplate;
        this.autorRepository = autorRepository;
        this.livroCategoriaRepository = livroCategoriaRepository;
        this.categoriaRepository = categoriaRepository;
        this.livroAutorRepository = livroAutorRepository;
        this.traducaoService = traducaoService;
    }


    @Transactional
    public void anunciarNovoLivro(LivroDTO livroDTO, Usuario Usuario) {
        Livro livro = new Livro(livroDTO);
        livroRepository.save(livro);
        for (String autor : livroDTO.getAutores()) {
            LivroAutor livroAutor = new LivroAutor();
            livroAutor.setLivro(livro);
            livroAutor.setAutor(autorRepository.findByNmAutor(autor).get());
            livroAutorRepository.save(livroAutor);
        }

        for (String categoria : livroDTO.getCategorias()) {
            LivroCategoria livroCategoria = new LivroCategoria();
            livroCategoria.setLivro(livro);
            livroCategoria.setCategoria(categoriaRepository.findByNmCategoriaTraduzida(categoria).get());
            livroCategoriaRepository.save(livroCategoria);
        }

        UsuarioLivro usuarioLivro = new UsuarioLivro();
        usuarioLivro.setLivro(livro);
        usuarioLivro.setUsuario(Usuario);
        usuarioLivro.setTipoNegociacao(UsuarioLivro.TipoNegociacao.valueOf(livroDTO.getTipoNegociacao()));
        usuarioLivroRepository.save(usuarioLivro);

    }

    @Transactional
    public List<LivroDTO> listarLivrosApi(String titulo) {
        GoogleBooksResponse respApi = this.buscarLivro(titulo);
        if (respApi == null || respApi.getItens() == null || respApi.getItens().isEmpty()) {
            return Collections.emptyList();
        }

        List<LivroDTO> livros = new ArrayList<>();
        for (GoogleBooksResponse.Item item : respApi.getItens()) {
            GoogleBooksResponse.VolumeInfo v = item.getVolumeInfo();
            if (v == null || v.getTitulo() == null){
                continue;
            }
            String tituloLivro = TextoUtils.normalizar(v.getTitulo());
            if (livros.stream().anyMatch(lDto -> lDto.getTitulo().equalsIgnoreCase(TextoUtils.normalizar(tituloLivro)))) {
                continue;
            }

            // Verifica se o livro já existe localmente
            Livro livroExistente = livroRepository.findByNmLivroIgnoreCase(v.getTitulo()).orElse(null);
            if (livroExistente != null) {
                LivroDTO livroDTO = new LivroDTO(livroExistente);
                livros.add(livroDTO);
                continue;
            }

            // Cria DTO a partir da API
            LivroDTO livro = new LivroDTO();
            livro.setTitulo(v.getTitulo());
            livro.setAutores((v.getAutores() == null || v.getAutores().isEmpty()) ? null : v.getAutores());
            livro.setCategorias((v.getCategorias() == null || v.getCategorias().isEmpty()) ? null : v.getCategorias());
            livro.setThumbnailUrl((v.getLinksImages() == null || v.getLinksImages().getThumbnail() == null)? null : TextoUtils.garantirHttps(v.getLinksImages().getThumbnail()));
            livro.setLingua((v.getLingua() == null || v.getLingua().isEmpty()) ? null : v.getLingua());
            String data = v.getDataPublicacao();
            try {
                if (data == null || data.isEmpty()) livro.setDataPublicacao(LocalDate.now());
                else if (data.length() == 4) livro.setDataPublicacao(LocalDate.parse(data + "-01-01"));
                else if (data.length() == 7) livro.setDataPublicacao(LocalDate.parse(data + "-01"));
                else livro.setDataPublicacao(LocalDate.parse(data));
            } catch (Exception e) {
                livro.setDataPublicacao(LocalDate.now());
            }

            // --- OTIMIZAÇÃO APLICADA ---
            // Tenta buscar dados faltantes usando a resposta que JÁ TEMOS, sem nova chamada de rede
            if (livro.getAutores() == null || livro.getCategorias() == null || livro.getThumbnailUrl() == null) {
                livro = buscarDadosFaltantes(livro, respApi);
            }
            String linguaOriginal = livro.getLingua() == null ? "en": livro.getLingua();

            boolean ehPortugues = linguaOriginal != null &&
                    (linguaOriginal.equalsIgnoreCase("pt") || linguaOriginal.equalsIgnoreCase("pt-br"));

            if (!ehPortugues) {
                livro.setTitulo(traducaoService.traduzirAPI(tituloLivro, linguaOriginal, "pt-br"));
            } else {
                livro.setTitulo(tituloLivro);
            }

            if (ehPortugues) {
                linguaOriginal = "en";
            }

            if (livro.getCategorias() == null || livro.getCategorias().isEmpty()) {
                livro.setCategorias(List.of("Categoria Não definida"));
            }

            if (livro.getAutores() == null || livro.getAutores().isEmpty()) {
                livro.setAutores(List.of("Autor Desconhecido"));
            }

            livro.setTituloFoiTraduzido(!ehPortugues);

            for (int i = 0; i < livro.getCategorias().size(); i++) {
                livro.getCategorias().set(i, traducaoService.buscarCategoriaTraduzida(TextoUtils.normalizar(livro.getCategorias().get(i)), linguaOriginal, "pt-BR"));
            }


            for (int i = 0; i < livro.getAutores().size(); i++) {
                livro.getAutores().set(i, TextoUtils.normalizar(livro.getAutores().get(i)));
            }

            for (String nmAutor : livro.getAutores()) {
                autorRepository.findByNmAutor(nmAutor).orElseGet(() -> autorRepository.save(new Autor(nmAutor)));
            }

            livros.add(livro);
        }
        return livros;
    }

    // --- ASSINATURA ALTERADA (Otimização) ---
    private LivroDTO buscarDadosFaltantes(LivroDTO dto, GoogleBooksResponse resp) {
        // Não faz nova chamada de rede, apenas re-varre a resposta original
        if (resp == null || resp.getItens() == null) return dto;

        for (GoogleBooksResponse.Item item : resp.getItens()) {
            GoogleBooksResponse.VolumeInfo v = item.getVolumeInfo();
            if (v == null) continue;

            // Preencher capa se vazia
            if (dto.getThumbnailUrl() == null) {
                if (v.getLinksImages() != null && v.getLinksImages().getThumbnail() != null) {
                    dto.setThumbnailUrl(TextoUtils.garantirHttps(v.getLinksImages().getThumbnail()));
                }
            }

            if (dto.getLingua() == null) {
                if (v.getLingua() != null) {
                    dto.setLingua(v.getLingua());
                }
            }

            // Preencher autores se vazio
            if (dto.getAutores() == null || dto.getAutores().isEmpty()) {
                if (v.getAutores() != null && !v.getAutores().isEmpty()) {
                    dto.setAutores(v.getAutores());
                }
            }

            // Preencher categorias se vazio
            if (dto.getCategorias() == null || dto.getCategorias().isEmpty()) {
                if (v.getCategorias() != null && !v.getCategorias().isEmpty()) {
                    dto.setCategorias(v.getCategorias());
                }
            }

            // Se tudo foi encontrado, pode parar
            if (dto.getThumbnailUrl() != null &&
                    dto.getAutores() != null && !dto.getAutores().isEmpty() &&
                    dto.getCategorias() != null && !dto.getCategorias().isEmpty() &&
                    dto.getLingua() != null)
            {
                return dto;
            }
        }

        // Se ainda assim faltar capa, define a padrão
        if (dto.getThumbnailUrl() == null) {
            dto.setThumbnailUrl("/img/default-capa.png");
        }

        return dto;
    }


    public GoogleBooksResponse buscarLivro(String titulo){
        // Usa a constante estática
        String urlCompleta = GOOGLE_BOOKS_API_URL + titulo;
        logger.info("Buscando livros na API: {}", urlCompleta);

        ResponseEntity<GoogleBooksResponse> resposta = restTemplate.exchange(
                urlCompleta,
                HttpMethod.GET,
                null,
                GoogleBooksResponse.class
        );

        if (resposta.getBody() != null && resposta.getBody().getItens() != null){
            logger.debug("Itens retornados da API: {}", resposta.getBody().getItens().size());
        } else {
            logger.warn("Chamada à API não retornou itens.");
        }

        return resposta.getBody();
    }
}*/