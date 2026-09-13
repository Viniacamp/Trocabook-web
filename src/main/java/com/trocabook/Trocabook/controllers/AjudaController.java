package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AjudaController {
	private final UsuarioAutenticadoService usuarioAutenticadoService;

	public AjudaController(UsuarioAutenticadoService usuarioAutenticadoService) {
		this.usuarioAutenticadoService = usuarioAutenticadoService;
	}

	@GetMapping("/ajuda")
	public String ajudaHome(HttpSession sessao, Model model) {
		UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(sessao);
		if (usuario != null) {
			model.addAttribute("usuario", usuario);
		}
		return "ajudahome";
	}
}
