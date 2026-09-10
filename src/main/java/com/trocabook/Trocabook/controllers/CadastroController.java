package com.trocabook.Trocabook.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import com.trocabook.Trocabook.model.dto.UsuarioCadastroDTO;
import com.trocabook.Trocabook.model.dto.UsuarioInput;
import com.trocabook.Trocabook.service.impl.FileStorageServiceUsuario;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.impl.RecaptchaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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

@Controller
public class CadastroController {

    private final IUsuarioService usuarioService;
    private final RecaptchaService recaptchaService;
    private final FileStorageServiceUsuario fileStorageService;

    @Value("${google.recaptcha.key.site}")
    private String recaptchaSiteKey;

    public CadastroController(
            IUsuarioService usuarioService,
            RecaptchaService recaptchaService,
            FileStorageServiceUsuario fileStorageService
    ) {
        this.usuarioService = usuarioService;
        this.recaptchaService = recaptchaService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {

        model.addAttribute(
                "usuarioDTO",
                new UsuarioCadastroDTO()
        );

        model.addAttribute(
                "recaptchaSiteKey",
                recaptchaSiteKey
        );

        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @RequestParam("fotoA") MultipartFile foto,
            @RequestParam("g-recaptcha-response") String recaptchaToken,
            @Valid @ModelAttribute("usuarioDTO") UsuarioCadastroDTO usuarioDTO,
            BindingResult result,
            Model model,
            RedirectAttributes attributes
    ) throws IOException, FirebaseAuthException {

        /*
         * 1. Validação do reCAPTCHA
         */
        boolean recaptchaValido =
                recaptchaService.verifyRecaptcha(recaptchaToken);

        if (!recaptchaValido) {

            attributes.addFlashAttribute(
                    "recaptchaError",
                    "Falha na verificação reCAPTCHA. Tente novamente."
            );

            return "redirect:/cadastro";
        }


        /*
         * 2. Validação da foto
         */
        if (foto.isEmpty()) {

            model.addAttribute(
                    "fotoErro",
                    "Selecione uma foto válida"
            );

            result.reject("fotoA");
        }


        /*
         * 3. Verificação de e-mail
         */
        if (usuarioService.existeComEmail(usuarioDTO.getEmail())) {

            result.rejectValue(
                    "email",
                    "email.exists",
                    "O Email inserido já está cadastrado no sistema"
            );
        }


        /*
         * 4. Verificação de CPF
         */
        if (usuarioService.existeComCpf(usuarioDTO.getCPF())) {

            result.rejectValue(
                    "CPF",
                    "cpf.exists",
                    "O CPF inserido já está cadastrado no sistema"
            );
        }


        /*
         * 5. Retorna para o formulário caso existam erros
         */
        if (result.hasErrors()) {

            model.addAttribute(
                    "recaptchaSiteKey",
                    recaptchaSiteKey
            );

            return "cadastro";
        }


        /*
         * 6. Salva a foto localmente
         */
        String caminhoDaFoto =
                fileStorageService.armazenarArquivoUsuario(foto);


        /*
         * 7. Cria o objeto de entrada para o Firebase
         *
         * Os campos que ainda não existem no formulário
         * são enviados como null.
         */
        UsuarioInput input =
                new UsuarioInput(
                        usuarioDTO.getNmUsuario(),
                        usuarioDTO.getCPF(),
                        usuarioDTO.getEmail(),
                        null,
                        usuarioDTO.getSenha(),
                        caminhoDaFoto,
                        null,
                        null,
                        null,
                        null
                );


        /*
         * 8. Cadastro no Firebase Authentication + Firestore
         */
        usuarioService.cadastrar(input);


        /*
         * 9. Cadastro concluído
         */
        return "redirect:/login";
    }
}

