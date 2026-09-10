package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.model.dto.NegociacaoDTO;
import com.trocabook.Trocabook.repository.NegociacaoRepository;
import com.trocabook.Trocabook.service.INegociacaoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NegociacaoService implements INegociacaoService {

    private final NegociacaoRepository negociacaoRepository;

    public NegociacaoService(
            NegociacaoRepository negociacaoRepository) {

        this.negociacaoRepository = negociacaoRepository;
    }

    @Override
    public NegociacaoDTO salvar(NegociacaoDTO negociacaoDTO) {

        Negociacao negociacao =
                Negociacao.from(negociacaoDTO);

        negociacao.setId(UUID.randomUUID().toString());

        negociacaoRepository.salvar(negociacao);

        return negociacao.paraDto();
    }

    @Override
    public NegociacaoDTO buscarPorUid(String uid) {

        Negociacao negociacao =
                negociacaoRepository.buscarPorUid(uid);

        if (negociacao == null) {
            return null;
        }

        return negociacao.paraDto();
    }

    @Override
    public List<NegociacaoDTO> listarPorUsuarioAnunciante(
            String uidAnunciante) {

        return negociacaoRepository
                .buscarPorUidUsuarioAnunciante(uidAnunciante)
                .stream()
                .map(Negociacao::paraDto)
                .toList();
    }

    @Override
    public List<NegociacaoDTO> listarPorUsuarioComprador(
            String uidComprador) {

        return negociacaoRepository
                .buscarPorUidUsuarioComprador(uidComprador)
                .stream()
                .map(Negociacao::paraDto)
                .toList();
    }

    @Override
    public List<NegociacaoDTO> listarPorUsuarioAnuncianteETipo(
            String uidAnunciante,
            Negociacao.TipoNegociacao tipoNegociacao) {

        return negociacaoRepository
                .buscarPorUidAnuncianteETipoNegociacao(
                        uidAnunciante,
                        tipoNegociacao
                )
                .stream()
                .map(Negociacao::paraDto)
                .toList();
    }

    @Override
    public List<NegociacaoDTO> listarPorUsuarioCompradorETipo(
            String uidComprador,
            Negociacao.TipoNegociacao tipoNegociacao) {

        return negociacaoRepository
                .buscarPorUidCompradorETipoNegociacao(
                        uidComprador,
                        tipoNegociacao
                )
                .stream()
                .map(Negociacao::paraDto)
                .toList();
    }

    @Override
    public long contarNegociacoesPorUsuarioETipo(String uidAnunciante, Negociacao.TipoNegociacao tipoNegociacao) {
        return negociacaoRepository.contarNegociacoesPorUsuarioETipo(uidAnunciante, tipoNegociacao);
    }
}