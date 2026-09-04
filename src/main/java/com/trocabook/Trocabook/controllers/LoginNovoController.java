package com.trocabook.Trocabook.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginNovoController {

	@Value("${google.recaptcha.key.site}")
	private String recaptchaSiteKey;

	@GetMapping("/loginNovo")
	public String login(Model model) { // Adicionado Model aqui

		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);

		return "loginNovo";
	}


}
