package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.NegociacaoDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.repository.NegociacaoRepository;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.INegociacaoService;
import com.trocabook.Trocabook.service.IUsuarioService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NegociacaoService implements INegociacaoService {

    private final NegociacaoRepository negociacaoRepository;
    private final IAnuncioService anuncioService;
    private final IUsuarioService usuarioService;

    public NegociacaoService(
            NegociacaoRepository negociacaoRepository,
            IAnuncioService anuncioService,
            IUsuarioService usuarioService) {

        this.negociacaoRepository = negociacaoRepository;
        this.anuncioService = anuncioService;
        this.usuarioService = usuarioService;
    }

    @Override
    public NegociacaoDTO criar(
            String anuncioId,
            String uidInteressado,
            Negociacao.TipoNegociacao tipoNegociacao
    ) {

        NegociacaoDTO negociacaoExistente =
                this.buscarPorAnuncioEComprador(
                        anuncioId,
                        uidInteressado
                );

        if (negociacaoExistente != null
                && !negociacaoExistente.status().equals("RECUSADA")
                && !negociacaoExistente.status().equals("CANCELADA")) {

            throw new IllegalStateException(
                    "Já existe uma negociação para este anúncio"
            );
        }

        AnuncioDTO anuncio =
                anuncioService.buscarPorUid(anuncioId);

        if (anuncio == null) {
            throw new IllegalArgumentException(
                    "Anúncio não encontrado"
            );
        }

        if (anuncio.uidUsuario().equals(uidInteressado)) {
            throw new IllegalArgumentException(
                    "O anunciante não pode negociar o próprio anúncio"
            );
        }

        validarTipoNegociacao(
                anuncio,
                tipoNegociacao
        );

        /*
         * Se já existia uma negociação encerrada,
         * reutiliza o mesmo documento.
         */
        if (negociacaoExistente != null) {

            Negociacao negociacaoAtualizada =
                    Negociacao.from(negociacaoExistente);

            negociacaoAtualizada.setStatus(
                    Negociacao.StatusNegociacao.PENDENTE
            );

            negociacaoAtualizada.setTipoNegociacao(
                    tipoNegociacao
            );

            negociacaoAtualizada.setDataNegociacao(
                    LocalDateTime.now().toString()
            );

            negociacaoAtualizada.setConfirmacaoAnunciante(false);

            negociacaoAtualizada.setConfirmacaoComprador(false);

            negociacaoRepository.salvar(
                    negociacaoAtualizada
            );

            return negociacaoAtualizada.paraDto();
        }

        UsuarioOutput comprador =
                usuarioService.buscarPorUid(uidInteressado);

        if (comprador == null) {
            throw new IllegalArgumentException(
                    "Usuário interessado não encontrado"
            );
        }

        Negociacao negociacao = new Negociacao();

        negociacao.setId(
                UUID.randomUUID().toString()
        );

        negociacao.setUsuarioAnuncianteId(
                anuncio.uidUsuario()
        );

        negociacao.setUsuarioCompradorId(
                uidInteressado
        );

        negociacao.setAnuncioId(
                anuncio.id()
        );

        negociacao.setDataNegociacao(
                LocalDateTime.now().toString()
        );

        negociacao.setTipoNegociacao(
                tipoNegociacao
        );

        negociacao.setStatus(
                Negociacao.StatusNegociacao.PENDENTE
        );

        negociacao.setNmAnunciante(
                anuncio.nomeUsuario()
        );

        negociacao.setFotoPerfilAnunciante(
                anuncio.fotoPerfil()
        );

        negociacao.setNmComprador(
                comprador.nome()
        );

        negociacao.setFotoPerfilComprador(
                comprador.fotoPerfil()
        );

        negociacao.setTitulo(
                anuncio.titulo()
        );

        negociacao.setCapa(
                anuncio.capa()
        );

        negociacao.setConfirmacaoAnunciante(false);

        negociacao.setConfirmacaoComprador(false);

        negociacaoRepository.salvar(
                negociacao
        );

        return negociacao.paraDto();
    }

    @Override
    public NegociacaoDTO buscarPorAnuncioEComprador(
            String anuncioId,
            String usuarioCompradorId
    ) {

        Negociacao negociacao =
                negociacaoRepository.buscarPorAnuncioEComprador(
                        anuncioId,
                        usuarioCompradorId
                );

        if (negociacao == null) {
            return null;
        }

        return negociacao.paraDto();
    }

    @Override
    public NegociacaoDTO aceitar(
            String uidNegociacao,
            String uidUsuario
    ) {

        Negociacao negociacao =
                negociacaoRepository.buscarPorUid(uidNegociacao);

        if (negociacao == null) {
            throw new IllegalArgumentException(
                    "Negociação não encontrada"
            );
        }

        if (!negociacao.getUsuarioAnuncianteId()
                .equals(uidUsuario)) {

            throw new SecurityException(
                    "Somente o anunciante pode aceitar a negociação"
            );
        }

        if (negociacao.getStatus()
                != Negociacao.StatusNegociacao.PENDENTE) {

            throw new IllegalStateException(
                    "A negociação não está pendente"
            );
        }

        boolean possuiNegociacaoEmAndamento =
                negociacaoRepository
                        .buscarPorAnuncioId(
                                negociacao.getAnuncioId()
                        )
                        .stream()
                        .anyMatch(n ->
                                n.getStatus()
                                        == Negociacao.StatusNegociacao.EM_ANDAMENTO
                        );

        if (possuiNegociacaoEmAndamento) {
            throw new IllegalStateException(
                    "Este anúncio já possui uma negociação em andamento"
            );
        }

        negociacao.setStatus(
                Negociacao.StatusNegociacao.EM_ANDAMENTO
        );

        negociacaoRepository.salvar(negociacao);

        return negociacao.paraDto();
    }

    @Override
    public NegociacaoDTO recusar(
            String uidNegociacao,
            String uidUsuario
    ) {

        Negociacao negociacao =
                negociacaoRepository.buscarPorUid(uidNegociacao);

        if (negociacao == null) {
            throw new IllegalArgumentException(
                    "Negociação não encontrada"
            );
        }

        if (!negociacao.getUsuarioAnuncianteId()
                .equals(uidUsuario)) {

            throw new SecurityException(
                    "Somente o anunciante pode recusar a negociação"
            );
        }

        if (negociacao.getStatus()
                != Negociacao.StatusNegociacao.PENDENTE) {

            throw new IllegalStateException(
                    "A negociação não está pendente"
            );
        }

        negociacao.setStatus(
                Negociacao.StatusNegociacao.RECUSADA
        );

        negociacaoRepository.salvar(negociacao);

        return negociacao.paraDto();
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

    private void validarTipoNegociacao(
            AnuncioDTO anuncio,
            Negociacao.TipoNegociacao tipoNegociacao
    ) {
        Negociacao.TipoNegociacao tipoAnuncio =
                Negociacao.TipoNegociacao.valueOf(
                        anuncio.tipoNegociacao()
                );

        if (tipoNegociacao == Negociacao.TipoNegociacao.AMBOS) {
            throw new IllegalArgumentException(
                    "A negociação deve ser definida como TROCA ou VENDA"
            );
        }

        if (tipoAnuncio != Negociacao.TipoNegociacao.AMBOS
                && tipoAnuncio != tipoNegociacao) {

            throw new IllegalArgumentException(
                    "Tipo de negociação incompatível com o anúncio"
            );
        }
    }
}