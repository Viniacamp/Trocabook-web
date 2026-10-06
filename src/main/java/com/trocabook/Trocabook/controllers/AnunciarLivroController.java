package com.trocabook.Trocabook.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.trocabook.Trocabook.service.impl.FileStorageServiceUsuario;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.trocabook.Trocabook.controllers.request.AnunciarLivroRequest;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AnunciarLivroController {

    private final ILivroService livroService;
    private final IAnuncioService anuncioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final FileStorageServiceUsuario fileStorageServiceUsuario;

    public AnunciarLivroController(ILivroService livroService, IAnuncioService anuncioService, UsuarioAutenticadoService usuarioAutenticadoService, FileStorageServiceUsuario fileStorageServiceUsuario) {
        this.livroService = livroService;
        this.anuncioService = anuncioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.fileStorageServiceUsuario = fileStorageServiceUsuario;
    }

    @GetMapping("/AnunciarLivro")
    public String anunciarLivroApi(Model model, HttpSession sessao) {
        UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(sessao);
        if (usuario == null){
            return "redirect:/";
        }
        configurarModel(model, new AnunciarLivroRequest(), usuario);

        return "anunciar";
    }



    @PostMapping("/AnunciarLivro")
    public String anunciar(
            @ModelAttribute("anuncioRequest") AnunciarLivroRequest request,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem,
            HttpSession session,
            Model model
    ) throws IOException {

        UsuarioOutput usuario =
                usuarioAutenticadoService.getUsuarioOutput(session);

        if (usuario == null) {
            return "redirect:/";
        }

        boolean manual =
                "MANUAL".equalsIgnoreCase(request.modoCadastro());

        boolean googleBooks =
                "GOOGLE_BOOKS".equalsIgnoreCase(request.modoCadastro());

        boolean temErro = false;

        if (!manual && !googleBooks) {
            model.addAttribute(
                    "erroCadastro",
                    "Selecione uma forma de cadastro válida."
            );
            temErro = true;
        }



        if (request.titulo() == null || request.titulo().isBlank()) {
            model.addAttribute(
                    "erroTitulo",
                    "O título do livro é obrigatório."
            );
            temErro = true;
        }

        if (request.tipoNegociacao() == null
                || request.tipoNegociacao().isBlank()) {

            model.addAttribute(
                    "erroTipo",
                    "Selecione o tipo de negociação."
            );
            temErro = true;
        }

        if (request.autores() == null
                || request.autores().isEmpty()) {

            model.addAttribute(
                    "erroAutor",
                    "O livro precisa ter pelo menos um autor."
            );
            temErro = true;
        }

        if (request.categorias() == null
                || request.categorias().isEmpty()) {

            model.addAttribute(
                    "erroCategoria",
                    "Selecione pelo menos uma categoria."
            );
            temErro = true;
        }

        /*
         * No cadastro manual, a capa vem de um MultipartFile.
         */
        if (manual) {

            if (imagem == null || imagem.isEmpty()) {
                model.addAttribute(
                        "erroFoto",
                        "Selecione uma imagem."
                );
                temErro = true;

            } else if (imagem.getContentType() == null
                    || !imagem.getContentType().startsWith("image/")) {

                model.addAttribute(
                        "erroFoto",
                        "O arquivo selecionado deve ser uma imagem."
                );
                temErro = true;
            }
        } else {

            /*
             * No cadastro pela Google Books, a capa já é uma URL.
             */
            if (request.urlImagem() == null
                    || request.urlImagem().isBlank()) {

                model.addAttribute(
                        "erroFoto",
                        "Selecione uma imagem."
                );
                temErro = true;
            }
        }

        if (temErro) {
            configurarModel(model, request, usuario);

            return "anunciar";
        }

        Livro livroSalvo;

        if (manual) {

            String urlImagem =
                    fileStorageServiceUsuario.armazenarArquivoLivro(imagem);

            livroSalvo =
                    livroService.cadastrarManual(
                            request.titulo(),
                            request.autores(),
                            request.categorias(),
                            urlImagem
                    );

        } else {

            livroSalvo =
                    livroService.cadastrar(
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

        anuncioService.anunciar(
                livroSalvo.getId(),
                usuario.id(),
                request.tipoNegociacao()
                        .toUpperCase(Locale.ROOT),
                request.descricao()
        );

        return "anuncioSucesso";
    }

    @PostMapping("/buscar")
    @ResponseBody
    public List<LivroBuscaOutput> buscar(@RequestBody LivroBuscaOutput livro) {
        System.out.println("Buscando livro");
        System.out.println(livro);
        return livroService.pesquisarLivros(livro.titulo());
    }

    private void configurarModel(Model model, AnunciarLivroRequest attributeValue, UsuarioOutput usuario) {
        model.addAttribute("anuncioRequest", attributeValue);
        model.addAttribute("usuario", usuario);
        model.addAttribute(
                "categoriasDisponiveis",
                livroService.listarCategorias()
        );
    }



}