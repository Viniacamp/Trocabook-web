package com.trocabook.Trocabook.controllers;


import com.trocabook.Trocabook.controllers.request.CriarNegociacaoRequest;
import com.trocabook.Trocabook.controllers.response.ChatAtualizacaoResponse;
import com.trocabook.Trocabook.controllers.response.ChatConversaResponse;
import com.trocabook.Trocabook.controllers.response.ChatResponse;


import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.model.dto.*;
import com.trocabook.Trocabook.service.*;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Locale;


@Controller
@RequestMapping("/chat")
public class ChatController {

    private final IConversaService conversaService;
    private final IUsuarioService usuarioService;
    private final IAnuncioService anuncioService;
    private final IInteracaoService interacaoService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final INegociacaoService negociacaoService;

    public ChatController(
            IConversaService conversaService,
            IUsuarioService usuarioService,
            IAnuncioService anuncioService,
            IInteracaoService interacaoService,
            UsuarioAutenticadoService usuarioAutenticadoService,
            INegociacaoService negociacaoService) {

        this.conversaService = conversaService;
        this.usuarioService = usuarioService;
        this.anuncioService = anuncioService;
        this.interacaoService = interacaoService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.negociacaoService = negociacaoService;
    }

    @GetMapping
    public String abrirChat(
            @RequestParam(name = "anuncio", required = false) String uidAnuncio,
            @RequestParam(name = "destinatario", required = false) String uidDestinatario,
            Model model,
            HttpSession sessao) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            return "redirect:/";
        }

        List<ConversaDTO> conversas =
                conversaService.listarConversas(usuarioLogado.id());

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("conversas", conversas);

        if (conversas.isEmpty()) {
            model.addAttribute("mensagemVazia", "Nenhuma conversa iniciada");
        }

        model.addAttribute("anuncioInicial", uidAnuncio);
        model.addAttribute("destinatarioInicial", uidDestinatario);

        return "/chat/chat";
    }

    @PostMapping("/negociacoes")
    @ResponseBody
    public ChatResponse<NegociacaoDTO> criarNegociacao(
            @RequestBody CriarNegociacaoRequest request,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException(
                    "Usuário não autenticado"
            );
        }

        Negociacao.TipoNegociacao tipoNegociacao;

        try {

            tipoNegociacao =
                    Negociacao.TipoNegociacao.valueOf(
                            request.tipoNegociacao()
                                    .trim()
                                    .toUpperCase(Locale.ROOT)
                    );

        } catch (
                IllegalArgumentException |
                NullPointerException e
        ) {

            throw new IllegalArgumentException(
                    "Tipo de negociação inválido"
            );
        }

        NegociacaoDTO negociacao =
                negociacaoService.criar(
                        request.anuncioId(),
                        usuarioLogado.id(),
                        tipoNegociacao
                );

        String conteudoMensagem =
                tipoNegociacao == Negociacao.TipoNegociacao.TROCA
                        ? "📚 Proposta de troca enviada para este livro."
                        : "📚 Proposta de compra enviada para este livro.";

        MensagemDTO mensagemProposta =
                new MensagemDTO(
                        null,
                        usuarioLogado.id(),
                        negociacao.usuarioAnuncianteId(),
                        request.anuncioId(),
                        conteudoMensagem,
                        null
                );

        conversaService.enviarMensagem(
                mensagemProposta
        );

        return new ChatResponse<>(
                negociacao,
                "sucesso"
        );
    }

    @PutMapping("/negociacoes/{id}/aceitar")
    @ResponseBody
    public ChatResponse<NegociacaoDTO> aceitarNegociacao(
            @PathVariable String id,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException(
                    "Usuário não autenticado"
            );
        }

        NegociacaoDTO negociacao =
                negociacaoService.aceitar(
                        id,
                        usuarioLogado.id()
                );

        MensagemDTO mensagemAceite =
                new MensagemDTO(
                        null,
                        usuarioLogado.id(),
                        negociacao.usuarioCompradorId(),
                        negociacao.anuncioId(),
                        "✅ Proposta aceita. A negociação está em andamento.",
                        null
                );

        conversaService.enviarMensagem(
                mensagemAceite
        );

        return new ChatResponse<>(
                negociacao,
                "sucesso"
        );
    }

    @PutMapping("/negociacoes/{id}/recusar")
    @ResponseBody
    public ChatResponse<NegociacaoDTO> recusarNegociacao(
            @PathVariable String id,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException(
                    "Usuário não autenticado"
            );
        }

        NegociacaoDTO negociacao =
                negociacaoService.recusar(
                        id,
                        usuarioLogado.id()
                );

        MensagemDTO mensagemRecusa =
                new MensagemDTO(
                        null,
                        usuarioLogado.id(),
                        negociacao.usuarioCompradorId(),
                        negociacao.anuncioId(),
                        "❌ Proposta recusada pelo anunciante.",
                        null
                );

        conversaService.enviarMensagem(
                mensagemRecusa
        );

        return new ChatResponse<>(
                negociacao,
                "sucesso"
        );
    }

    @PutMapping("/mensagens/{id}")
    @ResponseBody
    public ChatResponse<MensagemDTO> alterarMensagem(
            @PathVariable String id,
            @RequestBody AtualizarMensagemDTO conteudo,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException(
                    "Usuário não autenticado"
            );
        }

        MensagemDTO mensagemAtualizada =
                conversaService.atualizarMensagem(
                        id,
                        usuarioLogado.id(),
                        conteudo
                );

        return new ChatResponse<>(
                mensagemAtualizada,
                "sucesso"
        );
    }

    @DeleteMapping("/mensagens/{id}")
    @ResponseBody
    public ChatResponse<Void> excluirMensagem(
            @PathVariable String id,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException(
                    "Usuário não autenticado"
            );
        }

        conversaService.excluirMensagem(
                id,
                usuarioLogado.id()
        );

        return new ChatResponse<>(
                null,
                "sucesso"
        );
    }


    @PostMapping("/mensagens")
    @ResponseBody
    public ChatResponse<MensagemDTO> salvarMensagemAjax(
            @RequestBody MensagemDTO mensagemDTO,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException(
                    "Usuário não autenticado"
            );
        }

        if (mensagemDTO.uidAnuncio() == null
                || mensagemDTO.uidAnuncio().isBlank()) {

            throw new IllegalArgumentException(
                    "Anúncio não informado"
            );
        }

        if (mensagemDTO.uidDestinatario() == null
                || mensagemDTO.uidDestinatario().isBlank()) {

            throw new IllegalArgumentException(
                    "Destinatário não informado"
            );
        }

        if (mensagemDTO.conteudo() == null
                || mensagemDTO.conteudo().isBlank()) {

            throw new IllegalArgumentException(
                    "A mensagem não pode estar vazia"
            );
        }

        AnuncioDTO anuncio =
                anuncioService.buscarPorUid(
                        mensagemDTO.uidAnuncio()
                );

        if (anuncio == null) {
            throw new IllegalArgumentException(
                    "Anúncio não encontrado"
            );
        }

        boolean usuarioEhAnunciante =
                usuarioLogado.id()
                        .equals(anuncio.uidUsuario());

        if (usuarioEhAnunciante) {

            if (usuarioLogado.id()
                    .equals(mensagemDTO.uidDestinatario())) {

                throw new IllegalArgumentException(
                        "Não é possível enviar mensagem para si mesmo"
                );
            }

        } else {

            if (!mensagemDTO.uidDestinatario()
                    .equals(anuncio.uidUsuario())) {

                throw new SecurityException(
                        "Destinatário inválido para este anúncio"
                );
            }
        }

        MensagemDTO mensagemSegura =
                new MensagemDTO(
                        null,
                        usuarioLogado.id(),
                        mensagemDTO.uidDestinatario(),
                        anuncio.id(),
                        mensagemDTO.conteudo().trim(),
                        null
                );

        MensagemDTO mensagemSalva =
                conversaService.enviarMensagem(
                        mensagemSegura
                );

        if (!usuarioEhAnunciante) {

            interacaoService.registrarInicioConversa(
                    usuarioLogado.id(),
                    anuncio.uidLivro(),
                    anuncio.id()
            );
        }

        return new ChatResponse<>(
                mensagemSalva,
                "sucesso"
        );
    }

    @GetMapping("/conversa/atualizar")
    @ResponseBody
    public ChatResponse<ChatAtualizacaoResponse> atualizarConversa(
            @RequestParam("anuncio") String uidAnuncio,
            @RequestParam("destinatario") String uidUsuarioDestinatario,
            HttpSession sessao
    ) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException(
                    "Usuário não autenticado"
            );
        }

        AnuncioDTO anuncio =
                anuncioService.buscarPorUid(uidAnuncio);

        if (anuncio == null) {
            throw new IllegalArgumentException(
                    "Anúncio não encontrado"
            );
        }

        List<MensagemDTO> mensagens =
                conversaService.listarMensagens(
                        usuarioLogado.id(),
                        uidUsuarioDestinatario,
                        anuncio
                );

        String uidComprador;

        if (usuarioLogado.id().equals(anuncio.uidUsuario())) {
            uidComprador = uidUsuarioDestinatario;
        } else {
            uidComprador = usuarioLogado.id();
        }

        NegociacaoDTO negociacao =
                negociacaoService.buscarPorAnuncioEComprador(
                        uidAnuncio,
                        uidComprador
                );

        ChatAtualizacaoResponse resposta =
                new ChatAtualizacaoResponse(
                        mensagens,
                        negociacao
                );

        return new ChatResponse<>(
                resposta,
                "sucesso"
        );
    }

    @GetMapping("/conversar/dados")
    @ResponseBody
    public ChatResponse<ChatConversaResponse> carregarConversa(
            @RequestParam("anuncio") String uidAnuncio,
            @RequestParam("destinatario") String uidUsuarioDestinatario,
            HttpSession sessao
    ) {
        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado");
        }

        AnuncioDTO anuncio = anuncioService.buscarPorUid(uidAnuncio);

        if (anuncio == null) {
            throw new IllegalArgumentException("Anúncio não encontrado");
        }

        List<MensagemDTO> mensagens =
                conversaService.listarMensagens(
                        usuarioLogado.id(),
                        uidUsuarioDestinatario,
                        anuncio
                );

        UsuarioOutput usuarioNegociante;

        if (uidUsuarioDestinatario.equals(anuncio.uidUsuario())) {

            usuarioNegociante = new UsuarioOutput(
                    anuncio.uidUsuario(),
                    anuncio.nomeUsuario(),
                    anuncio.fotoPerfil()
            );

        } else {

            usuarioNegociante =
                    usuarioService.buscarPorUid(uidUsuarioDestinatario);
        }

        String uidComprador;

        if (usuarioLogado.id().equals(anuncio.uidUsuario())) {
            uidComprador = uidUsuarioDestinatario;
        } else {
            uidComprador = usuarioLogado.id();
        }

        NegociacaoDTO negociacao =
                negociacaoService.buscarPorAnuncioEComprador(
                        uidAnuncio,
                        uidComprador
                );

        ChatConversaResponse resposta =
                new ChatConversaResponse(
                        anuncio,
                        usuarioNegociante,
                        mensagens,
                        negociacao
                );

        return new ChatResponse<>(resposta, "sucesso");
    }

    @GetMapping("/conversas/atualizar")
    @ResponseBody
    public ChatResponse<List<ConversaDTO>> atualizarConversas(
            HttpSession sessao) {

        UsuarioOutput usuarioLogado =
                usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            throw new SecurityException("Usuário não autenticado");
        }

        List<ConversaDTO> conversas =
                conversaService.listarConversas(usuarioLogado.id());

        return new ChatResponse<>(
                conversas,
                "sucesso"
        );
    }
}
