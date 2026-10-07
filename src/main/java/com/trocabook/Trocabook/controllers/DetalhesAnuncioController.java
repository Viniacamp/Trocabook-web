package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.controllers.request.AtualizarAnuncioRequest;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.IInteracaoService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class DetalhesAnuncioController {

    private final IAnuncioService anuncioService;
    private final IInteracaoService interacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public DetalhesAnuncioController(
            IAnuncioService anuncioService,
            IInteracaoService interacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.anuncioService = anuncioService;
        this.interacaoService = interacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping("/anuncios/{id}")
    public String detalhes(
            @PathVariable String id,
            HttpSession sessao,
            Model model
    ) {
        AnuncioDTO anuncio = anuncioService.buscarPorUid(id);

        if (anuncio == null) {
            return "redirect:/";
        }

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        boolean proprioAnuncio = usuarioLogado != null
                && usuarioLogado.id().equals(anuncio.uidUsuario());

        if (usuarioLogado != null && !proprioAnuncio) {
            interacaoService.registrarVisualizacao(
                    usuarioLogado.id(),
                    anuncio.uidLivro(),
                    anuncio.id()
            );
        }

        model.addAttribute("anuncio", anuncio);
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("proprioAnuncio", proprioAnuncio);

        return "detalhes-anuncio";
    }

    @PostMapping("/anuncios/{id}/editar")
    public String editar(
            @PathVariable String id,
            @ModelAttribute AtualizarAnuncioRequest request,
            HttpSession sessao
    ) {
        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        try {
            anuncioService.atualizar(
                    id,
                    usuarioLogado.id(),
                    request.descricao(),
                    request.tipoNegociacao()
            );
        } catch (IllegalArgumentException e) {
            return "redirect:/anuncios/" + id;
        }

        return "redirect:/anuncios/" + id;
    }
}