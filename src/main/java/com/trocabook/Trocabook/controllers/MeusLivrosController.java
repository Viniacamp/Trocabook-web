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

    private final Set<String> tiposAnuncioValidos =
            Set.of(
                    "VENDA",
                    "TROCA",
                    "AMBOS"
            );

    private final Set<String> tiposNegociacaoValidos =
            Set.of(
                    "VENDA",
                    "TROCA"
            );

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

        if (tiposAnuncioValidos.contains(
                filtroAnuncio
        )) {

            Anuncio.TipoNegociacao tipo =
                    Anuncio.TipoNegociacao.valueOf(
                            filtroAnuncio
                    );

            livrosAnuncio =
                    anuncioService
                            .listarAnunciosAtivosPorUsuarioETipo(
                                    uidUsuario,
                                    tipo
                            );

        } else {

            livrosAnuncio =
                    anuncioService
                            .listarAnunciosAtivosPorUsuario(
                                    uidUsuario
                            );
        }

        List<NegociacaoDTO> negociacoes;

        if (tiposNegociacaoValidos.contains(
                filtroTroVen
        )) {

            negociacoes =
                    negociacaoService
                            .listarFinalizadasPorUsuarioETipo(
                                    uidUsuario,
                                    Negociacao.TipoNegociacao.valueOf(
                                            filtroTroVen
                                    )
                            );

        } else {

            negociacoes =
                    negociacaoService
                            .listarFinalizadasPorUsuario(
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