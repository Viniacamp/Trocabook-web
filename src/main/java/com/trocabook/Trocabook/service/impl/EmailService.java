package com.trocabook.Trocabook.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envia um e-mail com conteúdo HTML.
     * @param to E-mail do destinatário
     * @param subject Assunto do e-mail
     * @param htmlContent Corpo do e-mail em formato HTML
     */
    public void sendHtmlMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress("seu-email@gmail.com", "Trocabook"));

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) { // Adicione a nova exceção
            System.err.println("Erro ao enviar e-mail HTML: " + e.getMessage());
        }
    }
}