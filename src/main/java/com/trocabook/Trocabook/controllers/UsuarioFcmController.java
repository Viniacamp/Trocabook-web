package com.trocabook.Trocabook.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.trocabook.Trocabook.model.dto.AtualizarFcmTokenDTO;
import com.trocabook.Trocabook.service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
public class UsuarioFcmController {

    private final IUsuarioService usuarioService;

    public UsuarioFcmController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PutMapping("/token")
    public ResponseEntity<Void> atualizarFcmToken(
            @Valid @RequestBody AtualizarFcmTokenDTO dto,
            @AuthenticationPrincipal FirebaseToken token) {

        // Extrai o UID diretamente do token autenticado pelo seu filtro
        String uid = token.getUid();

        usuarioService.atualizarFcmToken(uid, dto.fcmToken());
        return ResponseEntity.noContent().build();
    }
}