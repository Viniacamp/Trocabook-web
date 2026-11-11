package com.trocabook.Trocabook.service.feign;

import com.trocabook.Trocabook.controllers.response.ChatResponse;
import com.trocabook.Trocabook.model.dto.AtualizarMensagemDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "chat-micro-servico-render",
        url = "${chat.service.fallback-url}/api/chat/mensagens"
)
public interface ChatRenderClient {

    @PostMapping
    ChatResponse<MensagemDTO> enviarMensagem(@RequestBody MensagemDTO mensagemDTO);

    @GetMapping("/{id}")
    ChatResponse<MensagemDTO> buscarMensagemPorId(@PathVariable("id") String id);

    @GetMapping
    ChatResponse<List<MensagemDTO>> listarMensagens(
            @RequestParam("remetente") String uidRemetente,
            @RequestParam(value = "destinatario", required = false) String uidDestinatario,
            @RequestParam(value = "anuncio", required = false) String uidAnuncio
    );

    @PutMapping("/{id}")
    ChatResponse<MensagemDTO> alterarMensagem(
            @PathVariable("id") String id,
            @RequestBody AtualizarMensagemDTO dto
    );

    @DeleteMapping("/{id}")
    ChatResponse<Void> excluirMensagem(@PathVariable("id") String id);
}