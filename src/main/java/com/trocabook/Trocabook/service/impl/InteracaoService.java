package com.trocabook.Trocabook.service.impl;

import com.google.cloud.Timestamp;
import com.trocabook.Trocabook.model.Interacao;
import com.trocabook.Trocabook.model.dto.InteracaoDTO;
import com.trocabook.Trocabook.repository.InteracaoRepository;
import com.trocabook.Trocabook.service.IInteracaoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class InteracaoService implements IInteracaoService {

    private final InteracaoRepository interacaoRepository;

    public InteracaoService(InteracaoRepository interacaoRepository) {
        this.interacaoRepository = interacaoRepository;
    }

    @Override
    public InteracaoDTO registrarPesquisa(
            String uidUsuario,
            String termoPesquisa
    ) {
        if (uidUsuario == null ||
                uidUsuario.isBlank() ||
                termoPesquisa == null ||
                termoPesquisa.isBlank()) {

            return null;
        }

        String termoNormalizado = termoPesquisa
                .trim()
                .toLowerCase(Locale.ROOT);

        Interacao interacao = new Interacao(
                UUID.randomUUID().toString(),
                uidUsuario,
                Interacao.TipoInteracao.PESQUISA,
                null,
                null,
                termoNormalizado,
                Interacao.OrigemInteracao.WEB,
                Timestamp.now()
        );

        interacaoRepository.salvar(interacao);

        return interacao.paraDto();
    }

    @Override
    public InteracaoDTO registrarVisualizacao(
            String uidUsuario,
            String uidLivro,
            String uidAnuncio
    ) {
        if (uidUsuario == null ||
                uidUsuario.isBlank() ||
                uidLivro == null ||
                uidLivro.isBlank() ||
                uidAnuncio == null ||
                uidAnuncio.isBlank()) {

            return null;
        }

        Interacao interacao = new Interacao(
                UUID.randomUUID().toString(),
                uidUsuario,
                Interacao.TipoInteracao.VISUALIZACAO,
                uidLivro,
                uidAnuncio,
                null,
                Interacao.OrigemInteracao.WEB,
                Timestamp.now()
        );

        interacaoRepository.salvar(interacao);

        return interacao.paraDto();
    }

    @Override
    public InteracaoDTO registrarInicioConversa(
            String uidUsuario,
            String uidLivro,
            String uidAnuncio
    ) {
        if (uidUsuario == null ||
                uidUsuario.isBlank() ||
                uidLivro == null ||
                uidLivro.isBlank() ||
                uidAnuncio == null ||
                uidAnuncio.isBlank()) {

            return null;
        }

        boolean inicioConversaJaRegistrado = !interacaoRepository
                .buscarPorUidUsuarioEAnuncioETipo(
                        uidUsuario,
                        uidAnuncio,
                        Interacao.TipoInteracao.INICIO_CONVERSA
                )
                .isEmpty();

        if (inicioConversaJaRegistrado) {
            return null;
        }

        Interacao interacao = new Interacao(
                UUID.randomUUID().toString(),
                uidUsuario,
                Interacao.TipoInteracao.INICIO_CONVERSA,
                uidLivro,
                uidAnuncio,
                null,
                Interacao.OrigemInteracao.WEB,
                Timestamp.now()
        );

        interacaoRepository.salvar(interacao);

        return interacao.paraDto();
    }

    @Override
    public List<InteracaoDTO> listarTodos() {
        return interacaoRepository
                .listarTodos()
                .stream()
                .map(Interacao::paraDto)
                .toList();
    }

    @Override
    public List<InteracaoDTO> listarPorUsuario(String uidUsuario) {
        return interacaoRepository
                .buscarPorUidUsuario(uidUsuario)
                .stream()
                .map(Interacao::paraDto)
                .toList();
    }
}