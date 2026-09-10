package com.trocabook.Trocabook.controllers;


import com.trocabook.Trocabook.config.ApplicationInstance;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class IndexController {

	private final UsuarioAutenticadoService usuarioAutenticadoService;
	private final IUsuarioService usuarioService;
	private final IAnuncioService anuncioService;
	private final ApplicationInstance applicationInstance;

	public IndexController(UsuarioAutenticadoService usuarioAutenticadoService, IUsuarioService usuarioService, IAnuncioService anuncioService, ApplicationInstance applicationInstance) {
		this.usuarioAutenticadoService = usuarioAutenticadoService;
		this.usuarioService = usuarioService;
		this.anuncioService = anuncioService;
		this.applicationInstance = applicationInstance;
	}

	@GetMapping("/")
	public String index(Model model, HttpSession sessao) {
		try {
			UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(sessao);

			model.addAttribute("usuario", usuario);
		} catch (IllegalStateException ex){

		}

		List<UsuarioOutput> destaques = usuarioService.buscarMelhoresAvaliados();
		List<AnuncioDTO> anuncios =
				anuncioService.listarTodos()
						.stream()
						.limit(10)
						.toList();

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
				.buscarPorTitulo(nm_livro);
	}
	
	

}
