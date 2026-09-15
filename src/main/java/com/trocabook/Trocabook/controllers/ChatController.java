package com.trocabook.Trocabook.controllers;


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

    @GetMapping("/conversar")
    public String conversar(
            @RequestParam("anuncio") String uidAnuncio,
            @RequestParam("remetente") String uidUsuarioRemetente,
            @RequestParam(name = "destinatario", required = false) String uidUsuarioDestinatario,
            HttpSession sessao,
            Model model
    ) {
        UsuarioOutput usuarioLogado = usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null) {
            return "redirect:/";
        }


        if (!uidUsuarioRemetente.equals(usuarioLogado.id())) {
            throw new SecurityException("Tentativa de acesso indevido a conversa de outro usuário");
        }

        AnuncioDTO anuncio = anuncioService.buscarPorUid(uidAnuncio);


        if (uidUsuarioDestinatario == null) {
            uidUsuarioDestinatario = anuncio.uidUsuario();
        }


        List<MensagemDTO> mensagens = conversaService.listarMensagens(uidUsuarioRemetente, uidUsuarioDestinatario, uidAnuncio);


        UsuarioOutput usuarioNegociante;

        if (uidUsuarioDestinatario.equals(anuncio.uidUsuario())) {
            usuarioNegociante = new UsuarioOutput(anuncio.uidUsuario(), anuncio.nomeUsuario(), anuncio.fotoPerfil());
        } else {
            usuarioNegociante = usuarioService.buscarPorUid(uidUsuarioDestinatario);
        }



        MensagemDTO mensagemDTO = new MensagemDTO(uidUsuarioRemetente, uidUsuarioDestinatario, uidAnuncio);

        model.addAttribute("mensagemDTO", mensagemDTO);
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("usuarioNegociante", usuarioNegociante);
        model.addAttribute("livro", anuncio);
        model.addAttribute("mensagens", mensagens);

        return "/chat/chat";
    }

    @GetMapping("/list-mensagens")
    public String listMensagens(Model model, HttpSession sessao) {
        UsuarioOutput usuarioLogado = usuarioAutenticadoService.getUsuarioOutput(sessao);

        if (usuarioLogado == null){
            return "redirect:/";
        }
        List<ConversaDTO> conversas = conversaService.listarConversas(usuarioLogado.id());

        if (conversas.isEmpty()) {
            model.addAttribute("mensagemVazia", "Nenhuma conversa iniciada");
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("conversas", conversas);

        return "/chat/list-mensagens";
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
}
