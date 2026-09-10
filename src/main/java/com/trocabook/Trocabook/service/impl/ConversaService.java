package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.dto.AtualizarMensagemDTO;
import com.trocabook.Trocabook.model.dto.ConversaDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.feign.ChatService;
import com.trocabook.Trocabook.service.IConversaService;
import com.trocabook.Trocabook.service.IUsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ConversaService implements IConversaService {

    private final ChatService chatService;
    private final IUsuarioService usuarioService;

    public ConversaService(
            ChatService chatService,
            IUsuarioService usuarioService) {

        this.chatService = chatService;
        this.usuarioService = usuarioService;
    }

    @Override
    public List<ConversaDTO> listarConversas(String uidUsuario) {

        return chatService
                .listarMensagensPorUsuarioDataEnvioDecrescente(uidUsuario)
                .getData()
                .stream()
                .map(m -> {

                    boolean enviadaPeloUsuarioLogado =
                            m.uidRemetente().equals(uidUsuario);

                    String uidDestinatario = enviadaPeloUsuarioLogado
                            ? m.uidDestinatario()
                            : m.uidRemetente();

                    UsuarioOutput destinatario =
                            usuarioService.buscarPorUid(uidDestinatario);

                    if (destinatario == null) {
                        return null;
                    }

                    ConversaDTO conversa = new ConversaDTO();

                    conversa.setUidAnuncio(m.uidAnuncio());
                    conversa.setUidDestinatario(destinatario.id());
                    conversa.setNomeDestinatario(destinatario.nome());
                    conversa.setFotoDestinatario(destinatario.fotoPerfil());
                    conversa.setUltimaMensagem(m.conteudo());
                    conversa.setEnviadaPeloUsuarioLogado(
                            enviadaPeloUsuarioLogado
                    );

                    return conversa;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<MensagemDTO> listarMensagens(
            String uidRemetente,
            String uidDestinatario,
            String uidAnuncio) {

        return chatService
                .listarMensagensEntreUsuarios(
                        uidRemetente,
                        uidDestinatario,
                        uidAnuncio
                )
                .getData();
    }

    @Override
    public MensagemDTO enviarMensagem(
            MensagemDTO mensagemDTO) {

        return chatService.enviarMensagem(mensagemDTO).getData();
    }

    @Override
    public MensagemDTO atualizarMensagem(
            String id,
            AtualizarMensagemDTO dto) {

        return chatService.alterarMensagem(id, dto).getData();
    }

    @Override
    public void excluirMensagem(String id) {
        chatService.excluirMensagem(id);
    }
}