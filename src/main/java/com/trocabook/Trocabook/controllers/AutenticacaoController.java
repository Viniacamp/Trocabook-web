package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public AutenticacaoController(
            UsuarioAutenticadoService usuarioAutenticadoService
    ) {
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @PostMapping("/autenticacao")
    public ResponseEntity<UsuarioOutput> autenticar(
            HttpSession sessao
    ) {

        UsuarioOutput usuario =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        return ResponseEntity.ok(usuario);
    }
}