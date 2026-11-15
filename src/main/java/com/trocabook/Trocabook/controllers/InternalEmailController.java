package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.service.impl.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/internal")
public class InternalEmailController {

    private final EmailService emailService;

    public InternalEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/enviar-email")
    public ResponseEntity<?> enviarEmailInterno(@RequestBody Map<String, String> payload) {
        try {
            String destinatario = payload.get("destinatario");
            String assunto = payload.get("assunto");
            String htmlContent = payload.get("htmlContent");

            emailService.sendHtmlMessage(destinatario, assunto, htmlContent);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao enviar e-mail: " + e.getMessage());
        }
    }
}
