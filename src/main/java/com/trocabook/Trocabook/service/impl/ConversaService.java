package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.dto.*;
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
            String uidUsuarioLogado,
            String uidDestinatario,
            AnuncioDTO anuncio
    ) {

        if (uidDestinatario == null
                || uidDestinatario.isBlank()) {

            throw new IllegalArgumentException(
                    "Destinatário não informado"
            );
        }

        if (uidUsuarioLogado.equals(uidDestinatario)) {

            throw new IllegalArgumentException(
                    "Não é possível acessar uma conversa consigo mesmo"
            );
        }

        boolean usuarioEhAnunciante =
                uidUsuarioLogado.equals(
                        anuncio.uidUsuario()
                );

        /*
         * Interessado:
         * só pode conversar com o anunciante.
         */
        if (!usuarioEhAnunciante
                && !uidDestinatario.equals(anuncio.uidUsuario())) {

            throw new SecurityException(
                    "Você não possui acesso a esta conversa"
            );
        }

        List<MensagemDTO> mensagens =
                chatService
                        .listarMensagensEntreUsuarios(
                                uidUsuarioLogado,
                                uidDestinatario,
                                anuncio.id()
                        )
                        .getData();

        /*
         * O interessado pode iniciar uma conversa nova,
         * portanto uma lista vazia é válida para ele.
         *
         * O anunciante só pode acessar uma conversa
         * que já tenha sido iniciada pelo interessado.
         */
        if (usuarioEhAnunciante
                && mensagens.isEmpty()) {

            throw new SecurityException(
                    "Você não possui acesso a esta conversa"
            );
        }

        return mensagens;
    }

    @Override
    public MensagemDTO enviarMensagem(
            MensagemDTO mensagemDTO) {

        return chatService.enviarMensagem(mensagemDTO).getData();
    }

    @Override
    public MensagemDTO atualizarMensagem(
            String id,
            String uidUsuario,
            AtualizarMensagemDTO dto
    ) {

        MensagemDTO mensagem =
                chatService
                        .buscarMensagemPorId(id)
                        .getData();

        if (mensagem == null) {
            throw new IllegalArgumentException(
                    "Mensagem não encontrada"
            );
        }

        if (!uidUsuario.equals(mensagem.uidRemetente())) {
            throw new SecurityException(
                    "Você não possui permissão para alterar esta mensagem"
            );
        }

        if (dto.conteudo() == null
                || dto.conteudo().isBlank()) {

            throw new IllegalArgumentException(
                    "A mensagem não pode estar vazia"
            );
        }

        AtualizarMensagemDTO mensagemAtualizada =
                new AtualizarMensagemDTO(
                        dto.conteudo().trim()
                );

        return chatService
                .alterarMensagem(
                        id,
                        mensagemAtualizada
                )
                .getData();
    }

    @Override
    public void excluirMensagem(
            String id,
            String uidUsuario
    ) {

        MensagemDTO mensagem =
                chatService
                        .buscarMensagemPorId(id)
                        .getData();

        if (mensagem == null) {
            throw new IllegalArgumentException(
                    "Mensagem não encontrada"
            );
        }

        if (!uidUsuario.equals(mensagem.uidRemetente())) {
            throw new SecurityException(
                    "Você não possui permissão para excluir esta mensagem"
            );
        }

        chatService.excluirMensagem(id);
    }
}