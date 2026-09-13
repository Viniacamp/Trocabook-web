package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class SobreNosController {
	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public SobreNosController(UsuarioAutenticadoService usuarioAutenticadoService) {
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@GetMapping("/sobreNos")
	public String sobreNos(HttpSession sessao, Model model) {
		UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(sessao);
		if (usuario != null) {
			model.addAttribute("usuario", usuario);
		}
		return "sobrenos";
	}

}
