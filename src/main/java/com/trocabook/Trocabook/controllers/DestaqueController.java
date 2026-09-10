package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.*;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.INegociacaoService;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class DestaqueController {
    private final IAnuncioService anuncioService;

    private final IUsuarioService usuarioService;

    private final INegociacaoService negociacaoService;

    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public DestaqueController(IAnuncioService anuncioService, IUsuarioService usuarioService, INegociacaoService negociacaoService, UsuarioAutenticadoService usuarioAutenticadoService) {
        this.anuncioService = anuncioService;
        this.usuarioService = usuarioService;
        this.negociacaoService = negociacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @GetMapping("/destaque/{uid}")
    public String destaque(HttpSession sessao, Model model, @PathVariable String uid) {
        UsuarioOutput usuarioLogado = usuarioAutenticadoService.getUsuarioOutput(sessao);
        if (usuarioLogado != null) {
            model.addAttribute("usuarioLogin", usuarioLogado);
        }
        UsuarioOutput usuarioDestaque = usuarioService.buscarPorUid(uid);
        if (usuarioDestaque == null) {
            return "redirect:/";
        }
        Long numeroTrocas = negociacaoService.contarNegociacoesPorUsuarioETipo(uid, Negociacao.TipoNegociacao.TROCA);
        Long numeroVendas = negociacaoService.contarNegociacoesPorUsuarioETipo(uid, Negociacao.TipoNegociacao.VENDA);
        List<AnuncioDTO> livrosDestaque = anuncioService.listarAnunciosUsuario(uid);

        model.addAttribute("usuarioDestaque", usuarioDestaque);
        model.addAttribute("qtd_trocas", numeroTrocas);
        model.addAttribute("qtd_vendas", numeroVendas);
        model.addAttribute("livros", livrosDestaque);

        return "destaque";
    }
}
