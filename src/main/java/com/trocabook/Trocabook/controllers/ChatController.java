package com.trocabook.Trocabook.controllers;


import com.trocabook.Trocabook.controllers.response.ChatConversaResponse;
import com.trocabook.Trocabook.controllers.response.ChatResponse;




import com.trocabook.Trocabook.model.dto.*;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.IConversaService;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequestMapping("/chat")
public class ChatController {

    private final IConversaService conversaService;
    private final IUsuarioService usuarioService;
    private final IAnuncioService anuncioService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ChatController(
            IConversaService conversaService,
            IUsuarioService usuarioService,
            IAnuncioService anuncioService,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.conversaService = conversaService;
        this.usuarioService = usuarioService;
        this.anuncioService = anuncioService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
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

    @PutMapping("/mensagens/{id}")
    @ResponseBody
    public ChatResponse<MensagemDTO> alterarMensagem(@PathVariable String id, @RequestBody AtualizarMensagemDTO conteudo){
        return new ChatResponse<>(conversaService.atualizarMensagem(id, conteudo), "sucesso");
    }

    @DeleteMapping("/mensagens/{id}")
    @ResponseBody
    public ChatResponse<Void> excluirMensagem(@PathVariable String id){
        conversaService.excluirMensagem(id);
        return new ChatResponse<>(null, "sucesso");
    }


    // 🔹 Envio de mensagem (via fetch)
    @PostMapping("/mensagens")
    @ResponseBody
    public ChatResponse<MensagemDTO> salvarMensagemAjax(@RequestBody MensagemDTO mensagemDTO) {
        return new ChatResponse<>(conversaService.enviarMensagem(mensagemDTO), "sucesso");
    }

    // 🔹 Atualização automática (polling)
    @GetMapping("/mensagens/atualizar")
    @ResponseBody
    public ChatResponse<List<MensagemDTO>> atualizarMensagens(
            @RequestParam("anuncio") String uidAnuncio,
            @RequestParam("remetente") String uidUsuarioRemetente,
            @RequestParam("destinatario") String uidUsuarioDestinatario
    ) {
        List<MensagemDTO> mensagens = conversaService.listarMensagens(uidUsuarioRemetente, uidUsuarioDestinatario, uidAnuncio);

        return new ChatResponse<>(mensagens, "sucesso");
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
                        uidAnuncio
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

        ChatConversaResponse resposta =
                new ChatConversaResponse(
                        anuncio,
                        usuarioNegociante,
                        mensagens
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
