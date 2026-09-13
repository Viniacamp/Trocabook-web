package com.trocabook.Trocabook.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.impl.EmailService;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.impl.FirebaseAuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class PasswordResetController {

    private final IUsuarioService usuarioService;
    private final FirebaseAuthService firebaseAuthService;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    public PasswordResetController(
            IUsuarioService usuarioService,
            FirebaseAuthService firebaseAuthService,
            EmailService emailService,
            TemplateEngine templateEngine) {

        this.usuarioService = usuarioService;
        this.firebaseAuthService = firebaseAuthService;
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/esqueci-senha")
    public String esqueciSenha() {
        return "esqueci-senha";
    }

    @PostMapping("/esqueci-senha")
    public String processarEsqueciSenha(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        UsuarioOutput usuario =
                usuarioService.buscarPorEmail(email);

        if (usuario == null) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não encontramos uma conta com esse e-mail."
            );

            return "redirect:/esqueci-senha";
        }

        try {

            String linkFirebase =
                    firebaseAuthService.gerarLinkRedefinicaoSenha(email);

            String oobCode =
                    extrairParametro(linkFirebase, "oobCode");

            if (oobCode == null || oobCode.isBlank()) {

                redirectAttributes.addFlashAttribute(
                        "erro",
                        "Não foi possível gerar o link de redefinição."
                );

                return "redirect:/esqueci-senha";
            }

            String resetUrl =
                    "http://localhost:8080/redefinir-senha?oobCode="
                            + URLEncoder.encode(
                            oobCode,
                            StandardCharsets.UTF_8
                    );

            Context context = new Context();

            context.setVariable(
                    "nomeUsuario",
                    usuario.nome()
            );

            context.setVariable(
                    "resetUrl",
                    resetUrl
            );

            String htmlEmail = templateEngine.process(
                    "email/email-redefinicao-senha",
                    context
            );

            emailService.sendHtmlMessage(
                    email,
                    "Redefinição de senha - Trocabook",
                    htmlEmail
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Enviamos um link de redefinição para o seu e-mail."
            );

        } catch (FirebaseAuthException e) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não foi possível gerar o link de redefinição."
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Ocorreu um erro ao processar a redefinição de senha."
            );
        }

        return "redirect:/esqueci-senha";
    }

    @GetMapping("/redefinir-senha")
    public String redefinirSenha(
            @RequestParam(
                    name = "oobCode",
                    required = false
            ) String oobCode) {

        if (oobCode == null || oobCode.isBlank()) {
            return "redirect:/esqueci-senha";
        }

        return "redefinir-senha";
    }

    private String extrairParametro(
            String link,
            String parametro) {

        URI uri = URI.create(link);

        String query = uri.getRawQuery();

        if (query == null || query.isBlank()) {
            return null;
        }

        for (String parte : query.split("&")) {

            String[] chaveValor = parte.split("=", 2);

            if (chaveValor.length == 2
                    && chaveValor[0].equals(parametro)) {

                return URLDecoder.decode(
                        chaveValor[1],
                        StandardCharsets.UTF_8
                );
            }
        }

        return null;
    }
}