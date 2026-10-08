package com.trocabook.Trocabook.controllers;


import java.util.List;

import com.trocabook.Trocabook.service.IInteracaoService;
import com.trocabook.Trocabook.service.IRecomendacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.trocabook.Trocabook.config.ApplicationInstance;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class IndexController {

	private final UsuarioAutenticadoService usuarioAutenticadoService;
	private final IUsuarioService usuarioService;
	private final IAnuncioService anuncioService;
	private final IInteracaoService interacaoService;
	private final IRecomendacaoService recomendacaoService;
	private final ApplicationInstance applicationInstance;

	public IndexController(UsuarioAutenticadoService usuarioAutenticadoService, IUsuarioService usuarioService, IAnuncioService anuncioService, IInteracaoService interacaoService, IRecomendacaoService recomendacaoService, ApplicationInstance applicationInstance) {
		this.usuarioAutenticadoService = usuarioAutenticadoService;
		this.usuarioService = usuarioService;
		this.anuncioService = anuncioService;
		this.interacaoService = interacaoService;
		this.recomendacaoService = recomendacaoService;
		this.applicationInstance = applicationInstance;
	}

	@GetMapping("/")
	public String index(Model model, HttpSession sessao) {

		List<AnuncioDTO> todosAnuncios =
				anuncioService.listarAtivos();

		List<AnuncioDTO> recomendacoes;

		try {
			UsuarioOutput usuario =
					usuarioAutenticadoService.getUsuarioOutput(sessao);

			model.addAttribute("usuario", usuario);

			recomendacoes =
					recomendacaoService.buscarRecomendacoes(
							usuario.id(),
							5,
							todosAnuncios
					);

		} catch (IllegalStateException ex) {
			recomendacoes =
					recomendacaoService.buscarAleatorios(
							todosAnuncios,
							5
					);
		}

		List<UsuarioOutput> destaques =
				usuarioService.buscarMelhoresAvaliados();

		List<AnuncioDTO> anuncios =
				todosAnuncios.stream()
						.limit(10)
						.toList();

		model.addAttribute("recomendacoes", recomendacoes);
		model.addAttribute("destaques", destaques);
		model.addAttribute("anuncios", anuncios);
		model.addAttribute(
				"applicationInstance",
				applicationInstance.getId()
		);

		return "index";
	}

	@PostMapping("/deslogar")
	public String deslogar(HttpSession sessao) {
		usuarioAutenticadoService.limparSessao(sessao);
		sessao.invalidate();
		return "redirect:/login";
	}


	@GetMapping("/pesquisar")
	@ResponseBody
	public List<AnuncioDTO> pesquisar(@RequestParam(name="titulo", required = false) String nm_livro){
		if (nm_livro == null || nm_livro.isBlank()) {
			return List.of();
		}
		return anuncioService
				.buscarAnunciosAtivosPorTitulo(nm_livro);
	}

	@PostMapping("/pesquisar/interacao")
	@ResponseBody
	public void registrarInteracaoPesquisa(
			@RequestParam String termo,
			HttpSession sessao) {

		if (termo == null || termo.isBlank()) {
			return;
		}

		try {
			UsuarioOutput usuario =
					usuarioAutenticadoService.getUsuarioOutput(sessao);

			if (usuario != null) {
				interacaoService.registrarPesquisa(
						usuario.id(),
						termo
				);
			}

		} catch (IllegalStateException ex) {
			// Usuário não autenticado:
			// não registra interação personalizada.
		}
	}
	

}
