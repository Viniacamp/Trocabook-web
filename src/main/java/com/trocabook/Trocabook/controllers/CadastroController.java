package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.dto.UsuarioCadastroDTO;
import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.repository.UsuarioRepository;
import com.trocabook.Trocabook.service.FileStorageServiceUsuario; // 1. Importar o serviço de arquivos
import com.trocabook.Trocabook.service.RecaptchaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
/*
@Controller
public class CadastroController {

    @Autowired
    private UsuarioRepository ur;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RecaptchaService recaptchaService;

    // --- PASSO 1: INJETAR O SERVIÇO DE ARQUIVOS ---
    @Autowired
    private FileStorageServiceUsuario fileStorageService;

    @Value("${google.recaptcha.key.site}")
    private String recaptchaSiteKey;

    @GetMapping("/cadastro")
    public String cadastro(Model model, HttpSession sessao) {
        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuarioLogado");
        if (usuarioLogado != null) {
            return "redirect:/";
        }
        model.addAttribute("usuarioDTO", new UsuarioCadastroDTO());
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam("fotoA") MultipartFile foto,
                            @RequestParam("g-recaptcha-response") String recaptchaToken,
                            @Valid @ModelAttribute("usuarioDTO") UsuarioCadastroDTO usuarioDTO,
                            BindingResult result,
                            Model model,
                            RedirectAttributes attributes) throws IOException {

        boolean isRecaptchaValid = recaptchaService.verifyRecaptcha(recaptchaToken);
        if (!isRecaptchaValid) {
            attributes.addFlashAttribute("recaptchaError", "Falha na verificação reCAPTCHA. Tente novamente.");
            return "redirect:/cadastro";
        }

        if (foto.isEmpty()) {
            model.addAttribute("fotoErro", "Selecione uma foto válida");
            result.reject("fotoA");
        }
        if (ur.findByEmail(usuarioDTO.getEmail()) != null) {
            result.rejectValue("email", "email.exists", "O Email inserido já está cadastrado no sistema");
        }
        if (ur.findByCPF(usuarioDTO.getCPF()) != null) {
            result.rejectValue("CPF", "cpf.exists", "O CPF inserido já está cadastrado no sistema");
        }
        if (result.hasErrors()) {
            model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
            return "cadastro";
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNmUsuario(usuarioDTO.getNmUsuario());
        novoUsuario.setEmail(usuarioDTO.getEmail());
        novoUsuario.setCPF(usuarioDTO.getCPF());

        String senhaCriptografada = passwordEncoder.encode(usuarioDTO.getSenha());
        novoUsuario.setSenha(senhaCriptografada);

        // --- PASSO 2: USAR O SERVIÇO PARA SALVAR A FOTO ---
        // Primeiro, salvamos o arquivo e obtemos o caminho (String) de volta.
        String caminhoDaFoto = fileStorageService.armazenarArquivoUsuario(foto);
        // Agora, usamos essa String para definir a foto no usuário.
        novoUsuario.setFoto(caminhoDaFoto);

        novoUsuario.setStatus('A');
        ur.save(novoUsuario);

        return "redirect:/login";
    }
}*/