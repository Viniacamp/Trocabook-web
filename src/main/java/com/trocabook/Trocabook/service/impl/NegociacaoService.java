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
            Negociacao.TipoNegociacao tipoNegociacao,
            String anuncioOferecidoId,
            String descricaoOferta
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

        if ("FINALIZADO".equals(anuncio.status())) {
            throw new IllegalStateException(
                    "Este anúncio já foi finalizado"
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

        DadosOferta dadosOferta =
                validarEPrepararOferta(
                        anuncio,
                        uidInteressado,
                        tipoNegociacao,
                        anuncioOferecidoId,
                        descricaoOferta
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

            atualizarDadosOferta(
                    negociacaoAtualizada,
                    dadosOferta
            );

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

        atualizarDadosOferta(
                negociacao,
                dadosOferta
        );

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
                negociacaoRepository.buscarPorUid(
                        uidNegociacao
                );

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

        /*
         * O anúncio principal ainda precisa estar
         * disponível no momento do aceite.
         */
        validarAnuncioPrincipalParaAceite(
                negociacao
        );

        /*
         * Se for uma troca com outro anúncio
         * vinculado, valida novamente esse anúncio.
         *
         * A situação dele pode ter mudado desde
         * a criação da proposta.
         */
        validarAnuncioOferecidoParaAceite(
                negociacao
        );

        negociacao.setStatus(
                Negociacao.StatusNegociacao.EM_ANDAMENTO
        );

        Negociacao negociacaoAtualizada =
                negociacaoRepository.salvar(
                        negociacao
                );

        return negociacaoAtualizada.paraDto();
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
    public NegociacaoDTO buscarPorUidParaUsuario(
            String uidNegociacao,
            String uidUsuario
    ) {

        Negociacao negociacao =
                negociacaoRepository.buscarPorUid(
                        uidNegociacao
                );

        if (negociacao == null) {
            throw new IllegalArgumentException(
                    "Negociação não encontrada"
            );
        }

        boolean usuarioEhAnunciante =
                uidUsuario.equals(
                        negociacao.getUsuarioAnuncianteId()
                );

        boolean usuarioEhComprador =
                uidUsuario.equals(
                        negociacao.getUsuarioCompradorId()
                );

        if (!usuarioEhAnunciante
                && !usuarioEhComprador) {

            throw new SecurityException(
                    "Usuário não autorizado a acessar esta negociação"
            );
        }

        if (negociacao.getStatus()
                == Negociacao.StatusNegociacao.PENDENTE) {

            throw new IllegalStateException(
                    "A proposta ainda não foi aceita"
            );
        }

        if (negociacao.getStatus()
                == Negociacao.StatusNegociacao.RECUSADA) {

            throw new IllegalStateException(
                    "Esta proposta foi recusada"
            );
        }



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

    @Override
    public NegociacaoDTO confirmar(
            String uidNegociacao,
            String uidUsuario
    ) {

        Negociacao negociacao =
                negociacaoRepository.buscarPorUid(
                        uidNegociacao
                );

        if (negociacao == null) {
            throw new IllegalArgumentException(
                    "Negociação não encontrada"
            );
        }

        if (negociacao.getStatus()
                != Negociacao.StatusNegociacao.EM_ANDAMENTO) {

            throw new IllegalStateException(
                    "A negociação não está em andamento"
            );
        }

        boolean usuarioEhAnunciante =
                uidUsuario.equals(
                        negociacao.getUsuarioAnuncianteId()
                );

        boolean usuarioEhComprador =
                uidUsuario.equals(
                        negociacao.getUsuarioCompradorId()
                );

        if (!usuarioEhAnunciante
                && !usuarioEhComprador) {

            throw new SecurityException(
                    "Usuário não autorizado a confirmar esta negociação"
            );
        }

        if (usuarioEhAnunciante) {

            if (negociacao.isConfirmacaoAnunciante()) {
                throw new IllegalStateException(
                        "O anunciante já confirmou esta negociação"
                );
            }

            negociacao.setConfirmacaoAnunciante(true);

        } else {

            if (negociacao.isConfirmacaoComprador()) {
                throw new IllegalStateException(
                        "O interessado já confirmou esta negociação"
                );
            }

            negociacao.setConfirmacaoComprador(true);
        }

        if (negociacao.isConfirmacaoAnunciante()
                && negociacao.isConfirmacaoComprador()) {

            finalizarNegociacao(
                    negociacao
            );
        }

        Negociacao negociacaoAtualizada =
                negociacaoRepository.salvar(
                        negociacao
                );

        return negociacaoAtualizada.paraDto();

    }

    @Override
    public NegociacaoDTO cancelar(
            String uidNegociacao,
            String uidUsuario
    ) {

        Negociacao negociacao =
                negociacaoRepository.buscarPorUid(
                        uidNegociacao
                );

        if (negociacao == null) {
            throw new IllegalArgumentException(
                    "Negociação não encontrada"
            );
        }

        if (negociacao.getStatus()
                != Negociacao.StatusNegociacao.EM_ANDAMENTO) {

            throw new IllegalStateException(
                    "Somente negociações em andamento podem ser canceladas"
            );
        }

        boolean usuarioEhAnunciante =
                uidUsuario.equals(
                        negociacao.getUsuarioAnuncianteId()
                );

        boolean usuarioEhComprador =
                uidUsuario.equals(
                        negociacao.getUsuarioCompradorId()
                );

        if (!usuarioEhAnunciante
                && !usuarioEhComprador) {

            throw new SecurityException(
                    "Usuário não autorizado a cancelar esta negociação"
            );
        }

        negociacao.setStatus(
                Negociacao.StatusNegociacao.CANCELADA
        );

        Negociacao negociacaoAtualizada =
                negociacaoRepository.salvar(
                        negociacao
                );

        return negociacaoAtualizada.paraDto();
    }

    @Override
    public List<NegociacaoDTO> listarFinalizadasPorUsuario(
            String uidUsuario
    ) {

        return negociacaoRepository
                .buscarFinalizadasPorUsuario(
                        uidUsuario
                )
                .stream()
                .map(Negociacao::paraDto)
                .toList();
    }

    @Override
    public List<NegociacaoDTO> listarFinalizadasPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao
    ) {

        if (tipoNegociacao
                == Negociacao.TipoNegociacao.AMBOS) {

            throw new IllegalArgumentException(
                    "O histórico deve ser filtrado por TROCA ou VENDA"
            );
        }

        return negociacaoRepository
                .buscarFinalizadasPorUsuarioETipo(
                        uidUsuario,
                        tipoNegociacao
                )
                .stream()
                .map(Negociacao::paraDto)
                .toList();
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

    private void cancelarNegociacoesPendentesDosAnuncios(
            Negociacao negociacaoFinalizada
    ) {

        cancelarNegociacoesPendentesDoAnuncio(
                negociacaoFinalizada.getAnuncioId(),
                negociacaoFinalizada.getId()
        );

        if (possuiAnuncioOferecido(
                negociacaoFinalizada
        )) {

            cancelarNegociacoesPendentesDoAnuncio(
                    negociacaoFinalizada.getAnuncioOferecidoId(),
                    negociacaoFinalizada.getId()
            );
        }
    }

    private void cancelarNegociacoesPendentesDoAnuncio(
            String anuncioId,
            String negociacaoFinalizadaId
    ) {

        List<Negociacao> comoAnuncioPrincipal =
                negociacaoRepository
                        .buscarPorAnuncioId(
                                anuncioId
                        );

        List<Negociacao> comoAnuncioOferecido =
                negociacaoRepository
                        .buscarPorAnuncioOferecidoId(
                                anuncioId
                        );

        comoAnuncioPrincipal.forEach(
                negociacao ->
                        cancelarSePendente(
                                negociacao,
                                negociacaoFinalizadaId
                        )
        );

        comoAnuncioOferecido.forEach(
                negociacao ->
                        cancelarSePendente(
                                negociacao,
                                negociacaoFinalizadaId
                        )
        );
    }

    private void cancelarSePendente(
            Negociacao negociacao,
            String negociacaoFinalizadaId
    ) {

        if (negociacao.getId()
                .equals(negociacaoFinalizadaId)) {
            return;
        }

        if (negociacao.getStatus()
                != Negociacao.StatusNegociacao.PENDENTE) {
            return;
        }

        negociacao.setStatus(
                Negociacao.StatusNegociacao.CANCELADA
        );

        negociacaoRepository.salvar(
                negociacao
        );
    }

    private DadosOferta validarEPrepararOferta(
            AnuncioDTO anuncioPrincipal,
            String uidInteressado,
            Negociacao.TipoNegociacao tipoNegociacao,
            String anuncioOferecidoId,
            String descricaoOferta
    ) {

        if (tipoNegociacao == Negociacao.TipoNegociacao.VENDA) {
            return new DadosOferta(
                    null,
                    null,
                    null,
                    null
            );
        }

        String descricaoNormalizada =
                descricaoOferta == null
                        ? null
                        : descricaoOferta.trim();

        if (descricaoNormalizada != null
                && descricaoNormalizada.length() > 500) {

            throw new IllegalArgumentException(
                    "A descrição da oferta deve possuir no máximo 500 caracteres."
            );
        }

        boolean possuiAnuncioOferecido =
                anuncioOferecidoId != null
                        && !anuncioOferecidoId.isBlank();

        if (!possuiAnuncioOferecido) {

            if (descricaoNormalizada == null
                    || descricaoNormalizada.isBlank()) {

                throw new IllegalArgumentException(
                        "Informe o livro que deseja oferecer para a troca."
                );
            }

            return new DadosOferta(
                    null,
                    null,
                    null,
                    descricaoNormalizada
            );
        }

        AnuncioDTO anuncioOferecido =
                anuncioService.buscarPorUid(
                        anuncioOferecidoId
                );

        if (anuncioOferecido == null) {
            throw new IllegalArgumentException(
                    "O anúncio oferecido não foi encontrado."
            );
        }

        if (anuncioPrincipal.id().equals(
                anuncioOferecido.id()
        )) {
            throw new IllegalArgumentException(
                    "O anúncio não pode ser oferecido em troca por ele mesmo."
            );
        }

        if (!uidInteressado.equals(
                anuncioOferecido.uidUsuario()
        )) {
            throw new SecurityException(
                    "O usuário não possui permissão para oferecer este anúncio."
            );
        }

        if (!"ATIVO".equals(
                anuncioOferecido.status()
        )) {
            throw new IllegalStateException(
                    "O anúncio oferecido não está ativo."
            );
        }

        boolean aceitaTroca =
                "TROCA".equals(
                        anuncioOferecido.tipoNegociacao()
                )
                        || "AMBOS".equals(
                        anuncioOferecido.tipoNegociacao()
                );

        if (!aceitaTroca) {
            throw new IllegalArgumentException(
                    "O anúncio oferecido não está disponível para troca."
            );
        }

        return new DadosOferta(
                anuncioOferecido.id(),
                anuncioOferecido.titulo(),
                anuncioOferecido.capa(),
                descricaoNormalizada
        );
    }

    private void atualizarDadosOferta(
            Negociacao negociacao,
            DadosOferta dadosOferta
    ) {
        negociacao.setAnuncioOferecidoId(
                dadosOferta.anuncioId()
        );

        negociacao.setTituloLivroOferecido(
                dadosOferta.titulo()
        );

        negociacao.setCapaLivroOferecido(
                dadosOferta.capa()
        );

        negociacao.setDescricaoOferta(
                dadosOferta.descricao()
        );
    }

    private void validarAnuncioPrincipalParaAceite(
            Negociacao negociacao
    ) {

        AnuncioDTO anuncio =
                anuncioService.buscarPorUid(
                        negociacao.getAnuncioId()
                );

        if (anuncio == null) {
            throw new IllegalArgumentException(
                    "O anúncio desta negociação não foi encontrado."
            );
        }

        if (!"ATIVO".equals(anuncio.status())) {
            throw new IllegalStateException(
                    "Este anúncio não está mais disponível."
            );
        }

        if (possuiNegociacaoEmAndamento(
                negociacao.getAnuncioId(),
                negociacao.getId()
        )) {

            throw new IllegalStateException(
                    "Este anúncio já está comprometido em outra negociação."
            );
        }
    }

    private void validarAnuncioOferecidoParaAceite(
            Negociacao negociacao
    ) {

        if (negociacao.getTipoNegociacao()
                != Negociacao.TipoNegociacao.TROCA) {
            return;
        }

        String anuncioOferecidoId =
                negociacao.getAnuncioOferecidoId();

        /*
         * Troca com livro não anunciado.
         * Não existe segundo anúncio para validar.
         */
        if (anuncioOferecidoId == null
                || anuncioOferecidoId.isBlank()) {
            return;
        }

        AnuncioDTO anuncioOferecido =
                anuncioService.buscarPorUid(
                        anuncioOferecidoId
                );

        if (anuncioOferecido == null) {
            throw new IllegalStateException(
                    "O anúncio oferecido não foi encontrado."
            );
        }

        if (!"ATIVO".equals(
                anuncioOferecido.status()
        )) {

            throw new IllegalStateException(
                    "O livro oferecido não está mais disponível."
            );
        }

        if (!negociacao.getUsuarioCompradorId()
                .equals(anuncioOferecido.uidUsuario())) {

            throw new SecurityException(
                    "O anúncio oferecido não pertence mais ao usuário que realizou a proposta."
            );
        }

        boolean aceitaTroca =
                "TROCA".equals(
                        anuncioOferecido.tipoNegociacao()
                )
                        || "AMBOS".equals(
                        anuncioOferecido.tipoNegociacao()
                );

        if (!aceitaTroca) {
            throw new IllegalStateException(
                    "O anúncio oferecido não está mais disponível para troca."
            );
        }

        if (possuiNegociacaoEmAndamento(
                anuncioOferecidoId,
                negociacao.getId()
        )) {

            throw new IllegalStateException(
                    "O livro oferecido já está comprometido em outra negociação."
            );
        }
    }

    private boolean possuiNegociacaoEmAndamento(
            String anuncioId,
            String negociacaoIgnoradaId
    ) {

        boolean comoAnuncioPrincipal =
                negociacaoRepository
                        .buscarPorAnuncioId(anuncioId)
                        .stream()
                        .anyMatch(negociacao ->
                                !negociacao.getId()
                                        .equals(negociacaoIgnoradaId)
                                        && negociacao.getStatus()
                                        == Negociacao.StatusNegociacao.EM_ANDAMENTO
                        );

        if (comoAnuncioPrincipal) {
            return true;
        }

        return negociacaoRepository
                .buscarPorAnuncioOferecidoId(
                        anuncioId
                )
                .stream()
                .anyMatch(negociacao ->
                        !negociacao.getId()
                                .equals(negociacaoIgnoradaId)
                                && negociacao.getStatus()
                                == Negociacao.StatusNegociacao.EM_ANDAMENTO
                );
    }

    private void finalizarNegociacao(
            Negociacao negociacao
    ) {

        negociacao.setStatus(
                Negociacao.StatusNegociacao.FINALIZADA
        );

        /*
         * Finaliza o anúncio principal.
         */
        anuncioService.finalizar(
                negociacao.getAnuncioId()
        );

        /*
         * Em uma troca com anúncio vinculado,
         * o livro oferecido também deixa de estar
         * disponível.
         */
        if (possuiAnuncioOferecido(negociacao)) {

            anuncioService.finalizar(
                    negociacao.getAnuncioOferecidoId()
            );
        }

        /*
         * Depois da conclusão, outras propostas
         * pendentes envolvendo os anúncios utilizados
         * não podem continuar disponíveis.
         */
        cancelarNegociacoesPendentesDosAnuncios(
                negociacao
        );
    }

    private boolean possuiAnuncioOferecido(
            Negociacao negociacao
    ) {

        return negociacao.getTipoNegociacao()
                == Negociacao.TipoNegociacao.TROCA
                && negociacao.getAnuncioOferecidoId() != null
                && !negociacao.getAnuncioOferecidoId().isBlank();
    }



    private record DadosOferta(
            String anuncioId,
            String titulo,
            String capa,
            String descricao
    ) {
    }
}