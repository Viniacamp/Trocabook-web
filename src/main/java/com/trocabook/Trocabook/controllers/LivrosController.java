package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.IRecomendacaoService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LivrosController {

    private final IAnuncioService anuncioService;
    private final IRecomendacaoService recomendacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public LivrosController(
            IAnuncioService anuncioService,
            IRecomendacaoService recomendacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.anuncioService = anuncioService;
        this.recomendacaoService = recomendacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping("/livros")
    public String livros(
            Model model,
            HttpSession sessao,
            @RequestParam(
                    value = "livroTipo",
                    defaultValue = "todos"
            ) String filtroLivros
    ) {
        UsuarioOutput usuarioOutput =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioOutput == null) {
            return "redirect:/";
        }

        List<AnuncioDTO> listaAnuncios;

        if (filtroLivros.equals("VENDA")
                || filtroLivros.equals("TROCA")
                || filtroLivros.equals("AMBOS")) {

            listaAnuncios =
                    anuncioService.listarAnunciosAtivosPorTipoNegociacao(
                            Anuncio.TipoNegociacao.valueOf(
                                    filtroLivros
                            )
                    );

        } else {
            listaAnuncios =
                    anuncioService.listarAtivos();
        }

        listaAnuncios =
                recomendacaoService.ordenarPorRecomendacao(
                        usuarioOutput.id(),
                        listaAnuncios
                );

        model.addAttribute(
                "usuario",
                usuarioOutput
        );

        model.addAttribute(
                "filtroLivros",
                filtroLivros
        );

        model.addAttribute(
                "listaAnuncios",
                listaAnuncios
        );

        return "livros";
    }
}