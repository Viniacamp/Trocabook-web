package com.trocabook.Trocabook.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AjudaController {

	private final UsuarioAutenticadoService usuarioAutenticadoService;
	private final RestTemplate restTemplate;

	public AjudaController(UsuarioAutenticadoService usuarioAutenticadoService) {
		this.usuarioAutenticadoService = usuarioAutenticadoService;
		this.restTemplate = new RestTemplate();
	}

	@GetMapping("/ajuda")
	public String ajudaHome(HttpSession sessao, Model model) {
		try {
			UsuarioOutput usuario = usuarioAutenticadoService.getUsuarioOutput(sessao);
			if (usuario != null) {
				model.addAttribute("usuario", usuario);
			}
		} catch (Exception e) {
			// Ignora caso não haja sessão ativa
		}
		return "ajudahome";
	}

	@PostMapping("/api/suporte/enviar")
	@ResponseBody
	public ResponseEntity<String> enviarDuvida(@RequestBody Map<String, String> payload, HttpSession sessao) {
		String nomeUsuario = "Usuário Trocabook";
		String emailUsuario = "";

		// 1. Tenta recuperar os dados do usuário autenticado pela sessão ou token do Firebase
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.getPrincipal() instanceof FirebaseToken token) {
				if (token.getName() != null && !token.getName().trim().isEmpty()) {
					nomeUsuario = token.getName();
				}
				if (token.getEmail() != null) {
					emailUsuario = token.getEmail();
				}
			} else {
				var usuarioOut = usuarioAutenticadoService.getUsuarioOutput(sessao);
				if (usuarioOut != null) {
					if (usuarioOut.nome() != null) {
						nomeUsuario = usuarioOut.nome();
					}
				}
			}
		} catch (Exception ignored) {}

		// Validação do payload
		if (payload == null) {
			return ResponseEntity.badRequest().body("Corpo da requisição inválido.");
		}

		String titulo = payload.get("titulo");
		String mensagem = payload.get("mensagem");

		if (mensagem == null || mensagem.trim().isEmpty()) {
			return ResponseEntity.badRequest().body("A mensagem não pode estar vazia.");
		}

		// Monta o payload que será enviado para o microsserviço Admin
		Map<String, String> bodyAdmin = new HashMap<>();
		bodyAdmin.put("nomeUsuario", nomeUsuario);
		bodyAdmin.put("emailUsuario", emailUsuario);
		bodyAdmin.put("titulo", (titulo != null && !titulo.trim().isEmpty()) ? titulo.trim() : "Sem título");
		bodyAdmin.put("mensagem", mensagem.trim());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// ABERTURA CORRETA DO GENÉRICO >
		HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(bodyAdmin, headers);


		String urlAdmin = "https://trocabook-admin-service.azurewebsites.net/api/admin/suporte";

		try {
			restTemplate.postForEntity(urlAdmin, requestEntity, Void.class);
			return ResponseEntity.ok().body("Dúvida enviada com sucesso!");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Erro ao comunicar com o serviço de administração: " + e.getMessage());
		}
	}
}