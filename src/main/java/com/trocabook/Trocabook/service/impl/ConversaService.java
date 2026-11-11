package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.dto.*;
import com.trocabook.Trocabook.controllers.response.ChatResponse;
import com.trocabook.Trocabook.service.feign.ChatService;
import com.trocabook.Trocabook.service.feign.ChatRenderClient; // Injeta o cliente do Render
import com.trocabook.Trocabook.service.IConversaService;
import com.trocabook.Trocabook.service.IUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ConversaService implements IConversaService {

    private static final Logger log = LoggerFactory.getLogger(ConversaService.class);

    private final ChatService chatService;
    private final ChatRenderClient chatRenderClient;
    private final IUsuarioService usuarioService;

    public ConversaService(
            ChatService chatService,
            ChatRenderClient chatRenderClient,
            IUsuarioService usuarioService) {
        this.chatService = chatService;
        this.chatRenderClient = chatRenderClient;
        this.usuarioService = usuarioService;
    }

    @Override
    public List<ConversaDTO> listarConversas(String uidUsuario) {
        ChatResponse<List<MensagemDTO>> response;
        try {
            // Tenta o Azure primeiro
            response = chatService.listarMensagens(uidUsuario, null, null);
        } catch (Exception e) {
            log.warn("Azure indisponível ao listar conversas ({}). Alternando para o Render...", e.getMessage());
            try {
                // Fallback para o Render
                response = chatRenderClient.listarMensagens(uidUsuario, null, null);
            } catch (Exception ex) {
                log.error("Render também falhou ao listar conversas: {}", ex.getMessage());
                response = new ChatResponse<>(Collections.emptyList(), "Serviço indisponível em ambas as nuvens.");
            }
        }

        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }

        return response.getData().stream()
                .map(m -> {
                    boolean enviadaPeloUsuarioLogado = m.uidRemetente().equals(uidUsuario);

                    String uidDestinatario = enviadaPeloUsuarioLogado
                            ? m.uidDestinatario()
                            : m.uidRemetente();

                    UsuarioOutput destinatario = usuarioService.buscarPorUid(uidDestinatario);

                    if (destinatario == null) {
                        return null;
                    }

                    ConversaDTO conversa = new ConversaDTO();
                    conversa.setUidAnuncio(m.uidAnuncio());
                    conversa.setUidDestinatario(destinatario.id());
                    conversa.setNomeDestinatario(destinatario.nome());
                    conversa.setFotoDestinatario(destinatario.fotoPerfil());
                    conversa.setUltimaMensagem(m.conteudo());
                    conversa.setEnviadaPeloUsuarioLogado(enviadaPeloUsuarioLogado);

                    return conversa;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<MensagemDTO> listarMensagens(
            String uidUsuarioLogado,
            String uidDestinatario,
            AnuncioDTO anuncio
    ) {
        if (uidDestinatario == null || uidDestinatario.isBlank()) {
            throw new IllegalArgumentException("Destinatário não informado");
        }

        if (uidUsuarioLogado.equals(uidDestinatario)) {
            throw new IllegalArgumentException("Não é possível acessar uma conversa consigo mesmo");
        }

        boolean usuarioEhAnunciante = uidUsuarioLogado.equals(anuncio.uidUsuario());

        if (!usuarioEhAnunciante && !uidDestinatario.equals(anuncio.uidUsuario())) {
            throw new SecurityException("Você não possui acesso a esta conversa");
        }

        ChatResponse<List<MensagemDTO>> response;
        try {
            response = chatService.listarMensagens(uidUsuarioLogado, uidDestinatario, anuncio.id());
        } catch (Exception e) {
            log.warn("Azure indisponível ao listar mensagens. Alternando para o Render...");
            try {
                response = chatRenderClient.listarMensagens(uidUsuarioLogado, uidDestinatario, anuncio.id());
            } catch (Exception ex) {
                log.error("Render também falhou ao listar mensagens: {}", ex.getMessage());
                response = new ChatResponse<>(Collections.emptyList(), "Serviço indisponível.");
            }
        }

        List<MensagemDTO> mensagens = (response != null && response.getData() != null)
                ? response.getData()
                : Collections.emptyList();

        if (usuarioEhAnunciante && mensagens.isEmpty()) {
            throw new SecurityException("Você não possui acesso a esta conversa");
        }

        return mensagens;
    }

    @Override
    public MensagemDTO enviarMensagem(MensagemDTO mensagemDTO) {
        try {
            return chatService.enviarMensagem(mensagemDTO).getData();
        } catch (Exception e) {
            log.warn("Azure indisponível ao enviar mensagem. Tentando Render...");
            return chatRenderClient.enviarMensagem(mensagemDTO).getData();
        }
    }

    @Override
    public MensagemDTO atualizarMensagem(String id, String uidUsuario, AtualizarMensagemDTO dto) {
        MensagemDTO mensagem;
        try {
            mensagem = chatService.buscarMensagemPorId(id).getData();
        } catch (Exception e) {
            mensagem = chatRenderClient.buscarMensagemPorId(id).getData();
        }

        if (mensagem == null) {
            throw new IllegalArgumentException("Mensagem não encontrada");
        }

        if (!uidUsuario.equals(mensagem.uidRemetente())) {
            throw new SecurityException("Você não possui permissão para alterar esta mensagem");
        }

        if (dto.conteudo() == null || dto.conteudo().isBlank()) {
            throw new IllegalArgumentException("A mensagem não pode estar vazia");
        }

        AtualizarMensagemDTO mensagemAtualizada = new AtualizarMensagemDTO(dto.conteudo().trim());

        try {
            return chatService.alterarMensagem(id, mensagemAtualizada).getData();
        } catch (Exception e) {
            return chatRenderClient.alterarMensagem(id, mensagemAtualizada).getData();
        }
    }

    @Override
    public void excluirMensagem(String id, String uidUsuario) {
        MensagemDTO mensagem;
        try {
            mensagem = chatService.buscarMensagemPorId(id).getData();
        } catch (Exception e) {
            mensagem = chatRenderClient.buscarMensagemPorId(id).getData();
        }

        if (mensagem == null) {
            throw new IllegalArgumentException("Mensagem não encontrada");
        }

        if (!uidUsuario.equals(mensagem.uidRemetente())) {
            throw new SecurityException("Você não possui permissão para excluir esta mensagem");
        }

        try {
            chatService.excluirMensagem(id);
        } catch (Exception e) {
            chatRenderClient.excluirMensagem(id);
        }
    }
}