package com.trocabook.Trocabook.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseInput;
import com.trocabook.Trocabook.service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/usuarios")
public class UsuarioController {
    private final IUsuarioService usuarioService;

    public UsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> cadastrar(@Valid @RequestBody UsuarioFirebaseInput input) throws FirebaseAuthException {
        usuarioService.cadastrar(input);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> usuarioAtual(
            Authentication authentication) {

        FirebaseToken firebaseToken =
                (FirebaseToken) authentication.getPrincipal();

        return ResponseEntity.ok(
                Map.of(
                        "uid", firebaseToken.getUid(),
                        "email", firebaseToken.getEmail()
                )
        );
    }
}
