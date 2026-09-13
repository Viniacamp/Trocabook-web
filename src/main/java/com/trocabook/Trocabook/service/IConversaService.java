package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.controllers.response.ChatResponse;
import com.trocabook.Trocabook.model.dto.AtualizarMensagemDTO;
import com.trocabook.Trocabook.model.dto.ConversaDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;

import java.util.List;

public interface IConversaService {

    List<ConversaDTO> listarConversas(String uidUsuario);

    List<MensagemDTO> listarMensagens(
            String uidRemetente,
            String uidDestinatario,
            String uidAnuncio
    );

    MensagemDTO enviarMensagem(MensagemDTO mensagemDTO);

    MensagemDTO atualizarMensagem(
            String id,
            AtualizarMensagemDTO dto
    );

    void excluirMensagem(String id);
}