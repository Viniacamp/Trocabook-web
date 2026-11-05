package com.trocabook.Trocabook.service.feign;

import com.trocabook.Trocabook.model.dto.UsuarioStatusNotificacaoDTO;
import org.springframework.http.ResponseEntity;

public class ChatNotificacaoServiceFallback implements ChatNotificacaoService{

    private final ChatNotificacaoRenderClient chatNotificacaoRenderClient;

    public ChatNotificacaoServiceFallback(ChatNotificacaoRenderClient chatNotificacaoRenderClient) {
        this.chatNotificacaoRenderClient = chatNotificacaoRenderClient;
    }

    @Override
    public ResponseEntity<Void> sincronizarStatusUsuario(UsuarioStatusNotificacaoDTO dto) {
        return chatNotificacaoRenderClient.sincronizarStatusUsuario(dto);
    }
}
