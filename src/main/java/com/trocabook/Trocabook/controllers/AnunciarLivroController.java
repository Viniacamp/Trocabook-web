package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.controllers.request.AnunciarLivroRequest;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Controller
public class AnunciarLivroController {


    private final ILivroService livroService;
    private final IAnuncioService anuncioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public AnunciarLivroController(ILivroService livroService, IAnuncioService anuncioService, UsuarioAutenticadoService usuarioAutenticadoService) {
        this.livroService = livroService;
        this.anuncioService = anuncioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping("/AnunciarLivro")
    public String anunciarLivroApi(Model model, HttpSession sessao) {
        if (usuarioAutenticadoService.getUsuarioOutput(sessao) == null){
            return "redirect:/";
        }
        model.addAttribute("anuncioRequest", new AnunciarLivroRequest());
        return "anunciar";
    }

    @PostMapping("/AnunciarLivro")
    public String anunciar(@ModelAttribute("anuncioRequest") AnunciarLivroRequest request, HttpSession session, Model model) throws IOException {

        UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(session);

        if (usuario == null){
            return "redirect:/";
        }

        boolean temErro = false;

        // --- Validações ---
        if (request.titulo() == null || request.titulo().isBlank()) {
            model.addAttribute("erroTitulo", "O título do livro é obrigatório.");
            temErro = true;
        }

        if (request.tipoNegociacao() == null || request.tipoNegociacao().isBlank()) {
            model.addAttribute("erroTipo", "Selecione o tipo de negociação.");
            temErro = true;
        }

        if (request.autores() == null || request.autores().isEmpty()) {
            model.addAttribute("erroAutor", "O livro precisa ter pelo menos um autor.");
            temErro = true;
        }

        if (request.categorias() == null || request.categorias().isEmpty()) {
            model.addAttribute("erroCategoria", "Selecione pelo menos uma categoria.");
            temErro = true;
        }

        if (request.urlImagem() == null || request.urlImagem().isBlank()) {
            model.addAttribute("erroFoto", "Selecione uma imagem");
            temErro = true;
        }

        // Se houver erros, volta pra página e mantém os dados digitados
        if (temErro) {
            model.addAttribute("anuncioRequest", request);
            return "anunciar";
        }
        Livro livroSalvo = livroService.cadastrar(
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

        anuncioService.anunciar(livroSalvo.getId(), usuario.id(), request.tipoNegociacao().toUpperCase(Locale.ROOT));

        return "anuncioSucesso";
    }

    @PostMapping("/buscar")
    @ResponseBody
    public List<LivroBuscaOutput> buscar(@RequestBody LivroBuscaOutput livro) {
        System.out.println("Buscando livro");
        System.out.println(livro);
        return livroService.pesquisarLivros(livro.titulo());
    }




}