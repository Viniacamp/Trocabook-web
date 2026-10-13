package com.trocabook.Trocabook.controllers;

import com.google.firebase.auth.FirebaseAuthException;
import com.trocabook.Trocabook.model.dto.AdminUsuarioInput;
import com.trocabook.Trocabook.model.dto.AdminUsuarioOutput;
import com.trocabook.Trocabook.service.IUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final IUsuarioService usuarioService;

    public AdminUsuarioController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUsuarioOutput>> buscarTodos() {
        return ResponseEntity.ok(
                usuarioService.buscarTodosComoAdmin()
        );
    }

    @GetMapping("/{uid}")
    public ResponseEntity<AdminUsuarioOutput> buscarPorUid(
            @PathVariable String uid
    ) {
        AdminUsuarioOutput usuario =
                usuarioService.buscarPorUidComoAdmin(uid);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(usuario);
    }

    @PatchMapping("/{uid}")
    public ResponseEntity<AdminUsuarioOutput> atualizar(
            @PathVariable String uid,
            @Valid @RequestBody AdminUsuarioInput input
    ) throws FirebaseAuthException {

        AdminUsuarioOutput usuario =
                usuarioService.atualizarComoAdmin(uid, input);

        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(usuario);
    }
}