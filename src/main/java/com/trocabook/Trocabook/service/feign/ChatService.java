package com.trocabook.Trocabook.service.feign;

import com.trocabook.Trocabook.controllers.response.ChatResponse;
import com.trocabook.Trocabook.model.dto.AtualizarMensagemDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "chat-micro-servico",
        url = "https://trocabookchatservice.onrender.com/api/chat/mensagens"
)
public interface ChatService {

    @PostMapping
    ChatResponse<MensagemDTO> enviarMensagem(
            @RequestBody MensagemDTO mensagemDTO
    );

    @GetMapping
    ChatResponse<List<MensagemDTO>> listarMensagensEntreUsuarios(
            @RequestParam("remetente") String uidRemetente,
            @RequestParam("destinatario") String uidDestinatario,
            @RequestParam("anuncio") String uidAnuncio
    );

    @GetMapping
    ChatResponse<List<MensagemDTO>> listarMensagensPorUsuarioDataEnvioDecrescente(
            @RequestParam("remetente") String uidRemetente
    );

    @PutMapping("/{id}")
    ChatResponse<MensagemDTO> alterarMensagem(
            @PathVariable String id,
            @RequestBody AtualizarMensagemDTO dto
    );

    @DeleteMapping("/{id}")
    ChatResponse<Void> excluirMensagem(
            @PathVariable String id
    );
}