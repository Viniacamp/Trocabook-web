package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.UsuarioLivro;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseOutput;
import com.trocabook.Trocabook.repository.LivroRepository;
import com.trocabook.Trocabook.repository.UsuarioLivroRepository;
import com.trocabook.Trocabook.repository.UsuarioRepository;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.trocabook.Trocabook.model.Usuario;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedList;
import java.util.List;

@Controller
public class IndexController {
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private LivroRepository livroRepository;
    @Autowired
    private UsuarioLivroRepository usuarioLivroRepository;

	@Autowired
	private UsuarioAutenticadoService usuarioAutenticadoService;

	@GetMapping("/")
	public String index(Model model, HttpSession sessao) {
		try {
			UsuarioFirebaseOutput usuario = usuarioAutenticadoService.getUsuarioOutput(sessao);

			model.addAttribute("usuario", usuario);
		} catch (IllegalStateException ex){

		}

		List<Usuario> destaques =
				usuarioRepository.findTop6ByOrderByAvaliacaoDesc();

		model.addAttribute("destaques", destaques);

		return "index";
	}
	
	@PostMapping("/deslogar")
	public String deslogar(HttpSession sessao) {
		sessao.invalidate();
		return "redirect:/";
	}

	@GetMapping("/pesquisar")
	@ResponseBody
	public LinkedList<String[]> pesquisar(@RequestParam(name="titulo", required = false) String nm_livro){
		List<Livro> livrosBusca =  livroRepository.findByNmLivroContainingIgnoreCase(nm_livro);
		List<UsuarioLivro> livrosUsuarioLivro = usuarioLivroRepository.findByLivroIn(livrosBusca);
		LinkedList<String[]> info = new LinkedList<>();
		for (UsuarioLivro usuarioLivro : livrosUsuarioLivro) {
			String[] infoLivro = new String[4];
			infoLivro[0] = usuarioLivro.getLivro().getCapa();
			infoLivro[1] = usuarioLivro.getLivro().getNmLivro();
			infoLivro[2] = usuarioLivro.getUsuario().getFoto();
			infoLivro[3] = Integer.toString(usuarioLivro.getCdUsuarioLivro());
			info.add(infoLivro);
		}
		return info;
	}
	
	

}
