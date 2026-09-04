package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.model.dto.PasswordResetDTO;
import com.trocabook.Trocabook.repository.UsuarioRepository;
import com.trocabook.Trocabook.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
/*
@Controller
public class PasswordResetController {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Exibe o formulário para solicitar a redefinição de senha
    @GetMapping("/esqueci-senha")
    public String showForgotPasswordForm() {
        return "esqueci-senha"; // Retorna a view esqueci-senha.html
    }

    @PostMapping("/esqueci-senha")
    public String processForgotPassword(@RequestParam("email") String email, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Não foi encontrado um usuário com este e-mail.");
            return "redirect:/esqueci-senha";
        }

        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        String resetUrl = getAppUrl(request) + "/redefinir-senha?token=" + token;

        // --- INÍCIO DA MODIFICAÇÃO ---

        // 1. Crie o contexto do Thymeleaf e adicione as variáveis
        Context context = new Context();
        context.setVariable("nomeUsuario", usuario.getNmUsuario());
        context.setVariable("resetUrl", resetUrl);

        // 2. Processe o template HTML para gerar o corpo do e-mail
        String htmlContent = templateEngine.process("email/email-redefinicao-senha", context);

        // 3. Envie o e-mail usando o novo método para HTML
        String subject = "Trocabook - Redefinição de Senha";
        emailService.sendHtmlMessage(usuario.getEmail(), subject, htmlContent);

        // --- FIM DA MODIFICAÇÃO ---

        redirectAttributes.addFlashAttribute("message", "Um link para redefinição de senha foi enviado para o seu e-mail.");
        return "redirect:/esqueci-senha";
    }

    // Exibe o formulário para redefinir a senha
    @GetMapping("/redefinir-senha")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetPasswordToken(token);

        if (usuarioOpt.isEmpty() || usuarioOpt.get().getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "O link de redefinição é inválido ou expirou.");
            return "redirect:/login";
        }

        PasswordResetDTO passwordResetDTO = new PasswordResetDTO();
        passwordResetDTO.setToken(token);

        model.addAttribute("passwordResetDto", passwordResetDTO); // O nome deve ser "passwordResetDto"
        return "redefinir-senha";
    }

    // Processa a redefinição da senha
    @PostMapping("/redefinir-senha")
    public String processResetPassword(@Valid @ModelAttribute("passwordResetDto") PasswordResetDTO passwordDto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) { // Adicionamos Model

        // 1. Validação básica de confirmação de senha
        if (!passwordDto.getSenha().equals(passwordDto.getConfirmaSenha())) {
            bindingResult.rejectValue("confirmaSenha", "Match", "As senhas não coincidem.");
        }

        // 2. Verifica se há erros de validação (do DTO ou da confirmação)
        if (bindingResult.hasErrors()) {
            model.addAttribute("passwordResetDto", passwordDto); // Devolve o DTO para manter os dados e o token
            return "redefinir-senha"; // Retorna para a página de redefinição mostrando os erros
        }

        // 3. Continua com a lógica se a validação passou
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetPasswordToken(passwordDto.getToken());
        if (usuarioOpt.isEmpty() || usuarioOpt.get().getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "O link de redefinição é inválido ou expirou.");
            return "redirect:/login";
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setSenha(passwordEncoder.encode(passwordDto.getSenha()));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiryDate(null);
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("message", "Sua senha foi redefinida com sucesso!");
        return "redirect:/login";
    }

    // Método auxiliar para obter a URL base da aplicação
    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}*/