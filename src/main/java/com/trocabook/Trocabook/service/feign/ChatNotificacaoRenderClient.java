package com.trocabook.Trocabook.service.feign;

import com.trocabook.Trocabook.model.dto.UsuarioStatusNotificacaoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "chat-micro-servico-render-notificacao",
        url = "${chat.service.fallback-url}/api/chat/notificacoes/status-usuario"
)
public interface ChatNotificacaoRenderClient {
    @PostMapping
    ResponseEntity<Void> sincronizarStatusUsuario(@RequestBody UsuarioStatusNotificacaoDTO dto);
}
