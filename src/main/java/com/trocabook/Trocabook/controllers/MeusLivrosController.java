package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.NegociacaoDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.INegociacaoService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
public class MeusLivrosController {

    private final IAnuncioService anuncioService;
    private final INegociacaoService negociacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    private final Set<String> tiposValidos =
            Set.of("VENDA", "TROCA", "AMBOS");

    public MeusLivrosController(
            IAnuncioService anuncioService,
            INegociacaoService negociacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.anuncioService = anuncioService;
        this.negociacaoService = negociacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping("/MeusLivros")
    public String meusLivros(
            Model model,
            @RequestParam(
                    value = "filtroAnuncio",
                    defaultValue = "todos"
            ) String filtroAnuncio,
            @RequestParam(
                    value = "filtroT/V",
                    defaultValue = "todos"
            ) String filtroTroVen,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        String uidUsuario = usuarioLogado.id();

        List<AnuncioDTO> livrosAnuncio;

        if (tiposValidos.contains(filtroAnuncio)) {

            Anuncio.TipoNegociacao tipo =
                    Anuncio.TipoNegociacao.valueOf(filtroAnuncio);

            livrosAnuncio =
                    anuncioService.listarAnunciosUsuarioETipo(
                            uidUsuario,
                            tipo
                    );

        } else {

            livrosAnuncio =
                    anuncioService.listarAnunciosUsuario(
                            uidUsuario
                    );
        }

        List<NegociacaoDTO> negociacoes;

        if (tiposValidos.contains(filtroTroVen)) {

            // Aqui depende de como seu enum de negociação
            // está definido atualmente.
            negociacoes =
                    negociacaoService.listarPorUsuarioAnuncianteETipo(
                            uidUsuario,
                            Negociacao.TipoNegociacao.valueOf(filtroTroVen)
                    );

        } else {

            negociacoes =
                    negociacaoService.listarPorUsuarioAnunciante(
                            uidUsuario
                    );
        }

        model.addAttribute("anuncios", livrosAnuncio);
        model.addAttribute("negociacoes", negociacoes);
        model.addAttribute(
                "filtroSelecionadoAnuncio",
                filtroAnuncio
        );
        model.addAttribute(
                "filtroSelecionadoTroVen",
                filtroTroVen
        );

        model.addAttribute("usuarioLogado", usuarioLogado);

        return "meusLivros";
    }
}