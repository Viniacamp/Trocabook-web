package com.trocabook.Trocabook.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.trocabook.Trocabook.controllers.request.AnunciarLivroRequest;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.impl.FileStorageServiceUsuario;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AnunciarLivroController {

    private final ILivroService livroService;
    private final IAnuncioService anuncioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final FileStorageServiceUsuario fileStorageServiceUsuario;

    public AnunciarLivroController(
            ILivroService livroService,
            IAnuncioService anuncioService,
            UsuarioAutenticadoService usuarioAutenticadoService,
            FileStorageServiceUsuario fileStorageServiceUsuario
    ) {
        this.livroService = livroService;
        this.anuncioService = anuncioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.fileStorageServiceUsuario = fileStorageServiceUsuario;
    }

    @GetMapping("/AnunciarLivro")
    public String anunciarLivroApi(Model model, HttpSession sessao) {
        UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(sessao);
        if (usuario == null) {
            return "redirect:/";
        }
        configurarModel(model, new AnunciarLivroRequest(), usuario);
        return "anunciar";
    }

    @PostMapping("/AnunciarLivro")
    @ResponseBody
    public ResponseEntity<?> anunciar(
            @ModelAttribute("anuncioRequest") AnunciarLivroRequest request,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem,
            HttpSession session
    ) throws IOException {

        UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(session);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Usuário não autenticado. Faça login novamente."));
        }

        Map<String, String> erros = new HashMap<>();

        boolean manual = "MANUAL".equalsIgnoreCase(request.modoCadastro());
        boolean googleBooks = "GOOGLE_BOOKS".equalsIgnoreCase(request.modoCadastro());

        if (!manual && !googleBooks) {
            erros.put("erroCadastro", "Selecione uma forma de cadastro válida.");
        }

        if (request.titulo() == null || request.titulo().isBlank()) {
            erros.put("erroTitulo", "O título do livro é obrigatório.");
        }

        if (request.tipoNegociacao() == null || request.tipoNegociacao().isBlank()) {
            erros.put("erroTipo", "Selecione o tipo de negociação.");
        }

        if (request.autores() == null || request.autores().isEmpty()) {
            erros.put("erroAutor", "O livro precisa ter pelo menos um autor.");
        }

        if (request.categorias() == null || request.categorias().isEmpty()) {
            erros.put("erroCategoria", "Selecione pelo menos uma categoria.");
        }

        if (manual) {
            if (imagem == null || imagem.isEmpty()) {
                erros.put("erroFoto", "Selecione uma imagem.");
            } else if (imagem.getContentType() == null || !imagem.getContentType().startsWith("image/")) {
                erros.put("erroFoto", "O arquivo selecionado deve ser uma imagem.");
            }
        } else {
            if (request.urlImagem() == null || request.urlImagem().isBlank()) {
                erros.put("erroFoto", "Selecione uma imagem.");
            }
        }


        if (!erros.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
        }

        Livro livroSalvo;

        if (manual) {
            String urlImagem = fileStorageServiceUsuario.armazenarArquivoLivro(imagem);
            livroSalvo = livroService.cadastrarManual(
                    request.titulo(),
                    request.autores(),
                    request.categorias(),
                    urlImagem
            );
        } else {
            livroSalvo = livroService.cadastrar(
                    new LivroBuscaOutput(
                            request.googleBooksId(),
                            request.titulo(),
                            request.autores(),
                            request.publicadora(),
                            request.dataPublicacao(),
                            request.urlImagem(),
                            request.lingua(),
                            request.categorias()
                    )
            );
        }

        AnuncioDTO anuncioDTO = anuncioService.anunciar(
                livroSalvo.getId(),
                usuario.id(),
                request.tipoNegociacao().toUpperCase(Locale.ROOT),
                request.descricao()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(anuncioDTO);
    }

    @PostMapping("/buscar")
    @ResponseBody
    public List<LivroBuscaOutput> buscar(@RequestBody LivroBuscaOutput livro) {
        return livroService.pesquisarLivros(livro.titulo());
    }

    private void configurarModel(Model model, AnunciarLivroRequest attributeValue, UsuarioOutput usuario) {
        model.addAttribute("anuncioRequest", attributeValue);
        model.addAttribute("usuario", usuario);
        model.addAttribute("categoriasDisponiveis", livroService.listarCategorias());
    }
}