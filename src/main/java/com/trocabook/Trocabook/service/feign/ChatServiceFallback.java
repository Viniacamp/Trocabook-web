package com.trocabook.Trocabook.service.feign;

import com.trocabook.Trocabook.controllers.response.ChatResponse;
import com.trocabook.Trocabook.model.dto.AtualizarMensagemDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatServiceFallback implements ChatService {

    private final ChatRenderClient chatRenderClient;

    public ChatServiceFallback(ChatRenderClient chatRenderClient) {
        this.chatRenderClient = chatRenderClient;
    }

    @Override
    public ChatResponse<MensagemDTO> enviarMensagem(MensagemDTO mensagemDTO) {
        return chatRenderClient.enviarMensagem(mensagemDTO);
    }

    @Override
    public ChatResponse<MensagemDTO> buscarMensagemPorId(String id) {
        return chatRenderClient.buscarMensagemPorId(id);
    }

    @Override
    public ChatResponse<List<MensagemDTO>> listarMensagensEntreUsuarios(String uidRemetente, String uidDestinatario, String uidAnuncio) {
        return chatRenderClient.listarMensagensEntreUsuarios(uidRemetente, uidDestinatario, uidAnuncio);
    }

    @Override
    public ChatResponse<List<MensagemDTO>> listarMensagensPorUsuarioDataEnvioDecrescente(String uidRemetente) {
        return chatRenderClient.listarMensagensPorUsuarioDataEnvioDecrescente(uidRemetente);
    }

    @Override
    public ChatResponse<MensagemDTO> alterarMensagem(String id, AtualizarMensagemDTO dto) {
        return chatRenderClient.alterarMensagem(id, dto);
    }

    @Override
    public ChatResponse<Void> excluirMensagem(String id) {
        return chatRenderClient.excluirMensagem(id);
    }
}