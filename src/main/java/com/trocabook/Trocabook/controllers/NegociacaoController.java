package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.dto.NegociacaoDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.INegociacaoService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/negociacoes")
public class NegociacaoController {

    private final INegociacaoService negociacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public NegociacaoController(
            INegociacaoService negociacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.negociacaoService = negociacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping("/{id}")
    public String visualizar(
            @PathVariable String id,
            HttpSession sessao,
            Model model
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        NegociacaoDTO negociacao =
                negociacaoService.buscarPorUidParaUsuario(
                        id,
                        usuarioLogado.id()
                );

        model.addAttribute(
                "usuarioLogado",
                usuarioLogado
        );

        model.addAttribute(
                "negociacao",
                negociacao
        );

        return "negociacao/controle";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(
            @PathVariable String id,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(
                        sessao
                );

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        negociacaoService.confirmar(
                id,
                usuarioLogado.id()
        );

        return "redirect:/negociacoes/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(
            @PathVariable String id,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(
                        sessao
                );

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        negociacaoService.cancelar(
                id,
                usuarioLogado.id()
        );

        return "redirect:/negociacoes/" + id;
    }
}