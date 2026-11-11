package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.controllers.request.CriarNegociacaoRequest;
import com.trocabook.Trocabook.controllers.response.ChatAtualizacaoResponse;
import com.trocabook.Trocabook.controllers.response.ChatConversaResponse;
import com.trocabook.Trocabook.controllers.response.ChatResponse;
import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.AtualizarMensagemDTO;
import com.trocabook.Trocabook.model.dto.ConversaDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;
import com.trocabook.Trocabook.model.dto.NegociacaoDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.IConversaService;
import com.trocabook.Trocabook.service.IInteracaoService;
import com.trocabook.Trocabook.service.INegociacaoService;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.impl.UsuarioAutenticadoService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Suíte de testes unitários para a classe {@link ChatController}.
 * Atende às especificações da Matriz de Risco (REQ10 - Chat de Alto Risco) e aos Casos de Teste BDD (CT01 a CT18).
 * Utiliza estritamente o padrão BDD (// DADO, // QUANDO, // ENTÃO) e tratamento defensivo
 * com try-catch e fail (sem assertThrows).
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private IConversaService conversaService;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private IAnuncioService anuncioService;

    @Mock
    private IInteracaoService interacaoService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private INegociacaoService negociacaoService;

    @Mock
    private HttpSession sessao;

    @Mock
    private Model model;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(chatController, "chatServiceUrl", "http://localhost:8282");
        ReflectionTestUtils.setField(chatController, "chatServiceFallbackUrl", "http://localhost:8283");
    }

    private UsuarioOutput criarUsuarioLogado(String id, String nome) {
        return new UsuarioOutput(id, nome, "http://foto-" + id + ".jpg");
    }

    private AnuncioDTO criarAnuncioExemplo(String id, String uidUsuario) {
        return new AnuncioDTO(
                id,
                uidUsuario,
                "livro-55",
                "Vendedor Teste",
                "TROCA",
                "O Alquimista",
                "http://foto-vendedor.jpg",
                "http://capa.jpg",
                List.of("Paulo Coelho"),
                List.of("Ficção"),
                "Livro excelente",
                "ATIVO"
        );
    }

    private NegociacaoDTO criarNegociacaoExemplo(String id, String anuncianteId, String compradorId, String anuncioId, String tipo) {
        return new NegociacaoDTO(
                id,
                anuncianteId,
                compradorId,
                anuncioId,
                "2026-11-09T20:00:00",
                null,
                tipo,
                "PENDENTE",
                "Nome Anunciante",
                "http://foto-anunciante.jpg",
                "Nome Comprador",
                "http://foto-comprador.jpg",
                "O Alquimista",
                "http://capa.jpg",
                false,
                false,
                null,
                null,
                null,
                "Proposta de teste"
        );
    }

    // =========================================================================
    // 1. ABRIR CHAT
    // =========================================================================
    @Nested
    @DisplayName("Testes do endpoint abrirChat (GET /chat)")
    class AbrirChatTests {

        @Test
        @DisplayName("Deve redirecionar para '/' quando o usuário não estiver autenticado")
        void deveRedirecionarParaHome_quandoUsuarioNaoEstiverAutenticado() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO
            String view = chatController.abrirChat("anuncio-1", "dest-1", model, sessao);

            // ENTÃO
            assertEquals("redirect:/", view);
            verify(usuarioAutenticadoService).getUsuarioOutput(sessao);
            verifyNoInteractions(conversaService);
            verifyNoInteractions(model);
        }

        @Test
        @DisplayName("Deve carregar tela de chat com conversas quando usuário autenticado possuir conversas")
        void deveCarregarTelaDeChatComConversas_quandoUsuarioEstiverAutenticado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Pedro");
            ConversaDTO conversa = new ConversaDTO();
            conversa.setUidAnuncio("anuncio-1");
            conversa.setNomeDestinatario("Maria");
            conversa.setUltimaMensagem("Combinado!");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(conversaService.listarConversas("user-1")).thenReturn(List.of(conversa));

            // QUANDO
            String view = chatController.abrirChat("anuncio-1", "dest-1", model, sessao);

            // ENTÃO
            assertEquals("chat/chat", view);
            verify(model).addAttribute("usuarioLogado", usuarioLogado);
            verify(model).addAttribute("conversas", List.of(conversa));
            verify(model).addAttribute("chatServiceUrl", "http://localhost:8282");
            verify(model).addAttribute("chatServiceFallbackUrl", "http://localhost:8283");
            verify(model).addAttribute("anuncioInicial", "anuncio-1");
            verify(model).addAttribute("destinatarioInicial", "dest-1");
            verify(model, never()).addAttribute(eq("mensagemVazia"), any());
        }

        @Test
        @DisplayName("Deve exibir mensagem de lista vazia quando o usuário autenticado não possuir conversas")
        void deveExibirMensagemVazia_quandoUsuarioNaoPossuirConversas() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Pedro");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(conversaService.listarConversas("user-1")).thenReturn(Collections.emptyList());

            // QUANDO
            String view = chatController.abrirChat(null, null, model, sessao);

            // ENTÃO
            assertEquals("chat/chat", view);
            verify(model).addAttribute("conversas", Collections.emptyList());
            verify(model).addAttribute("mensagemVazia", "Nenhuma conversa iniciada");
        }
    }

    // =========================================================================
    // 2. CRIAR NEGOCIAÇÃO (CT01, CT17)
    // =========================================================================
    @Nested
    @DisplayName("Testes do endpoint criarNegociacao (POST /chat/negociacoes)")
    class CriarNegociacaoTests {

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando o usuário não estiver autenticado ao criar negociação")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoCriarNegociacao() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);
            CriarNegociacaoRequest request = new CriarNegociacaoRequest("anuncio-1", "TROCA", "livro-2", "Oferta");

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.criarNegociacao(request, sessao);
                fail("Deveria ter lançado SecurityException para usuário deslogado");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(negociacaoService);
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o tipo de negociação for inválido")
        void deveLancarIllegalArgumentException_quandoTipoNegociacaoForInvalidoAoCriarNegociacao() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("comprador-1", "João");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            CriarNegociacaoRequest requestInvalido = new CriarNegociacaoRequest("anuncio-1", "DOACAO_INVALIDA", null, null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.criarNegociacao(requestInvalido, sessao);
                fail("Deveria ter lançado IllegalArgumentException para tipo de negociação inválido");
            } catch (IllegalArgumentException e) {
                assertEquals("Tipo de negociação inválido", e.getMessage());
            }

            verifyNoInteractions(negociacaoService);
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT01 - Deve criar negociação de TROCA com sucesso e enviar mensagem automática correspondente")
        void deveCriarNegociacaoDeTrocaComSucesso_eEnviarMensagemAutomatica() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("comprador-1", "João");
            CriarNegociacaoRequest request = new CriarNegociacaoRequest("anuncio-1", "TROCA", "livro-ofertado-9", "Troca pelo meu livro");
            NegociacaoDTO negociacaoSalva = criarNegociacaoExemplo("neg-10", "anunciante-1", "comprador-1", "anuncio-1", "TROCA");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(negociacaoService.criar("anuncio-1", "comprador-1", Negociacao.TipoNegociacao.TROCA, "livro-ofertado-9", "Troca pelo meu livro"))
                    .thenReturn(negociacaoSalva);

            // QUANDO
            ChatResponse<NegociacaoDTO> resposta = chatController.criarNegociacao(request, sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals("sucesso", resposta.getMessage());
            assertEquals("neg-10", resposta.getData().id());

            ArgumentCaptor<MensagemDTO> captor = ArgumentCaptor.forClass(MensagemDTO.class);
            verify(conversaService).enviarMensagem(captor.capture());
            MensagemDTO mensagemEnviada = captor.getValue();
            assertEquals("comprador-1", mensagemEnviada.uidRemetente());
            assertEquals("anunciante-1", mensagemEnviada.uidDestinatario());
            assertEquals("anuncio-1", mensagemEnviada.uidAnuncio());
            assertEquals("📚 Proposta de troca enviada para este livro.", mensagemEnviada.conteudo());
        }

        @Test
        @DisplayName("CT01 - Deve criar negociação de VENDA com sucesso e enviar mensagem automática correspondente")
        void deveCriarNegociacaoDeVendaComSucesso_eEnviarMensagemAutomatica() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("comprador-1", "João");
            CriarNegociacaoRequest request = new CriarNegociacaoRequest("anuncio-1", "VENDA", null, "Quero comprar");
            NegociacaoDTO negociacaoSalva = criarNegociacaoExemplo("neg-20", "anunciante-1", "comprador-1", "anuncio-1", "VENDA");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(negociacaoService.criar("anuncio-1", "comprador-1", Negociacao.TipoNegociacao.VENDA, null, "Quero comprar"))
                    .thenReturn(negociacaoSalva);

            // QUANDO
            ChatResponse<NegociacaoDTO> resposta = chatController.criarNegociacao(request, sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals("neg-20", resposta.getData().id());

            ArgumentCaptor<MensagemDTO> captor = ArgumentCaptor.forClass(MensagemDTO.class);
            verify(conversaService).enviarMensagem(captor.capture());
            MensagemDTO mensagemEnviada = captor.getValue();
            assertEquals("📚 Proposta de compra enviada para este livro.", mensagemEnviada.conteudo());
        }
    }

    // =========================================================================
    // 3. ACEITAR / RECUSAR NEGOCIAÇÃO (CT01, CT17)
    // =========================================================================
    @Nested
    @DisplayName("Testes de aceite e recusa de negociações")
    class AceitarRecusarNegociacaoTests {

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não estiver autenticado ao aceitar negociação")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoAceitarNegociacao() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.aceitarNegociacao("neg-1", sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(negociacaoService);
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT01 - Deve aceitar negociação com sucesso e enviar mensagem informativa ao comprador")
        void deveAceitarNegociacaoComSucesso_eEnviarMensagemInformativa() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("anunciante-1", "Vendedor");
            NegociacaoDTO negociacaoAceita = criarNegociacaoExemplo("neg-1", "anunciante-1", "comprador-1", "anuncio-1", "TROCA");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(negociacaoService.aceitar("neg-1", "anunciante-1")).thenReturn(negociacaoAceita);

            // QUANDO
            ChatResponse<NegociacaoDTO> resposta = chatController.aceitarNegociacao("neg-1", sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals("sucesso", resposta.getMessage());

            ArgumentCaptor<MensagemDTO> captor = ArgumentCaptor.forClass(MensagemDTO.class);
            verify(conversaService).enviarMensagem(captor.capture());
            MensagemDTO msg = captor.getValue();
            assertEquals("anunciante-1", msg.uidRemetente());
            assertEquals("comprador-1", msg.uidDestinatario());
            assertEquals("anuncio-1", msg.uidAnuncio());
            assertEquals("✅ Proposta aceita. A negociação está em andamento.", msg.conteudo());
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não estiver autenticado ao recusar negociação")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoRecusarNegociacao() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.recusarNegociacao("neg-1", sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(negociacaoService);
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT01 - Deve recusar negociação com sucesso e enviar mensagem informativa ao comprador")
        void deveRecusarNegociacaoComSucesso_eEnviarMensagemInformativa() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("anunciante-1", "Vendedor");
            NegociacaoDTO negociacaoRecusada = criarNegociacaoExemplo("neg-1", "anunciante-1", "comprador-1", "anuncio-1", "TROCA");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(negociacaoService.recusar("neg-1", "anunciante-1")).thenReturn(negociacaoRecusada);

            // QUANDO
            ChatResponse<NegociacaoDTO> resposta = chatController.recusarNegociacao("neg-1", sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());

            ArgumentCaptor<MensagemDTO> captor = ArgumentCaptor.forClass(MensagemDTO.class);
            verify(conversaService).enviarMensagem(captor.capture());
            MensagemDTO msg = captor.getValue();
            assertEquals("anunciante-1", msg.uidRemetente());
            assertEquals("comprador-1", msg.uidDestinatario());
            assertEquals("❌ Proposta recusada pelo anunciante.", msg.conteudo());
        }
    }

    // =========================================================================
    // 4. ALTERAR E EXCLUIR MENSAGEM (CT10, CT14, CT17)
    // =========================================================================
    @Nested
    @DisplayName("Testes de alteração e exclusão de mensagens")
    class AlterarExcluirMensagemTests {

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não autenticado tentar alterar mensagem")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoAlterarMensagem() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.alterarMensagem("msg-1", new AtualizarMensagemDTO("Novo"), sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT10 - Deve alterar mensagem com sucesso quando usuário for o autor")
        void deveAlterarMensagemComSucesso_quandoUsuarioEstiverAutenticado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("autor-1", "Lucas");
            AtualizarMensagemDTO dto = new AtualizarMensagemDTO("Texto retificado");
            MensagemDTO mensagemAtualizada = new MensagemDTO("msg-1", "autor-1", "dest-1", "anuncio-1", "Texto retificado", LocalDateTime.now());

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(conversaService.atualizarMensagem("msg-1", "autor-1", dto)).thenReturn(mensagemAtualizada);

            // QUANDO
            ChatResponse<MensagemDTO> resposta = chatController.alterarMensagem("msg-1", dto, sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals("sucesso", resposta.getMessage());
            assertEquals("Texto retificado", resposta.getData().conteudo());

            verify(conversaService).atualizarMensagem("msg-1", "autor-1", dto);
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não autenticado tentar excluir mensagem")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoExcluirMensagem() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.excluirMensagem("msg-1", sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT14 - Deve excluir mensagem com sucesso quando usuário estiver autenticado")
        void deveExcluirMensagemComSucesso_quandoUsuarioEstiverAutenticado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("autor-1", "Lucas");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);

            // QUANDO
            ChatResponse<Void> resposta = chatController.excluirMensagem("msg-1", sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals("sucesso", resposta.getMessage());
            assertNull(resposta.getData());

            verify(conversaService).excluirMensagem("msg-1", "autor-1");
        }
    }

    // =========================================================================
    // 5. SALVAR MENSAGEM AJAX (CT01, CT02, CT05, CT17)
    // =========================================================================
    @Nested
    @DisplayName("Testes do endpoint salvarMensagemAjax (POST /chat/mensagens)")
    class SalvarMensagemAjaxTests {

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não estiver autenticado ao salvar mensagem via AJAX")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticado() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);
            MensagemDTO dto = new MensagemDTO("remet-1", "dest-1", "anuncio-1");

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.salvarMensagemAjax(dto, sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(anuncioService);
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT01 - Deve lançar IllegalArgumentException quando anúncio não for informado")
        void deveLancarIllegalArgumentException_quandoAnuncioNaoInformado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Nome");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            MensagemDTO dto = new MensagemDTO(null, "user-1", "dest-1", "   ", "Olá", null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.salvarMensagemAjax(dto, sessao);
                fail("Deveria ter lançado IllegalArgumentException para anúncio não informado");
            } catch (IllegalArgumentException e) {
                assertEquals("Anúncio não informado", e.getMessage());
            }

            verifyNoInteractions(anuncioService);
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT01 - Deve lançar IllegalArgumentException quando destinatário não for informado")
        void deveLancarIllegalArgumentException_quandoDestinatarioNaoInformado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Nome");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            MensagemDTO dto = new MensagemDTO(null, "user-1", null, "anuncio-1", "Olá", null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.salvarMensagemAjax(dto, sessao);
                fail("Deveria ter lançado IllegalArgumentException para destinatário nulo");
            } catch (IllegalArgumentException e) {
                assertEquals("Destinatário não informado", e.getMessage());
            }

            verifyNoInteractions(anuncioService);
        }

        @Test
        @DisplayName("CT01 - Deve lançar IllegalArgumentException quando o conteúdo da mensagem for nulo ou vazio")
        void deveLancarIllegalArgumentException_quandoConteudoForVazio() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Nome");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            MensagemDTO dto = new MensagemDTO(null, "user-1", "dest-1", "anuncio-1", "   ", null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.salvarMensagemAjax(dto, sessao);
                fail("Deveria ter lançado IllegalArgumentException para conteúdo vazio");
            } catch (IllegalArgumentException e) {
                assertEquals("A mensagem não pode estar vazia", e.getMessage());
            }

            verifyNoInteractions(anuncioService);
        }

        @Test
        @DisplayName("CT05 - Deve lançar IllegalArgumentException quando o anúncio não for encontrado")
        void deveLancarIllegalArgumentException_quandoAnuncioNaoForEncontrado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Nome");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(anuncioService.buscarPorUid("anuncio-inexistente")).thenReturn(null);

            MensagemDTO dto = new MensagemDTO(null, "user-1", "dest-1", "anuncio-inexistente", "Olá", null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.salvarMensagemAjax(dto, sessao);
                fail("Deveria ter lançado IllegalArgumentException para anúncio não encontrado");
            } catch (IllegalArgumentException e) {
                assertEquals("Anúncio não encontrado", e.getMessage());
            }

            verify(anuncioService).buscarPorUid("anuncio-inexistente");
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT02 - Deve lançar IllegalArgumentException quando anunciante tentar enviar mensagem para si mesmo")
        void deveLancarIllegalArgumentException_quandoAnuncianteEnviarMensagemParaSiMesmo() {
            // DADO
            UsuarioOutput anunciante = criarUsuarioLogado("user-anunciante", "Dono");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "user-anunciante");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(anunciante);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);

            MensagemDTO dto = new MensagemDTO(null, "user-anunciante", "user-anunciante", "anuncio-1", "Teste próprio", null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.salvarMensagemAjax(dto, sessao);
                fail("Deveria ter lançado IllegalArgumentException para autoenvio");
            } catch (IllegalArgumentException e) {
                assertEquals("Não é possível enviar mensagem para si mesmo", e.getMessage());
            }

            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando comprador informar destinatário que não é o anunciante")
        void deveLancarSecurityException_quandoCompradorEnviarParaDestinatarioInvalido() {
            // DADO
            UsuarioOutput comprador = criarUsuarioLogado("user-comprador", "Comprador");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "dono-legitimo");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(comprador);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);

            MensagemDTO dto = new MensagemDTO(null, "user-comprador", "terceiro-estranho", "anuncio-1", "Tentativa indevida", null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.salvarMensagemAjax(dto, sessao);
                fail("Deveria ter lançado SecurityException para destinatário inválido");
            } catch (SecurityException e) {
                assertEquals("Destinatário inválido para este anúncio", e.getMessage());
            }

            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT01 - Deve salvar mensagem com sucesso quando comprador enviar mensagem e registrar interação")
        void deveSalvarMensagemComSucesso_quandoCompradorEnviar_eRegistrarInteracao() {
            // DADO
            UsuarioOutput comprador = criarUsuarioLogado("comprador-1", "Gabriel");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "anunciante-1");
            MensagemDTO dtoEnvio = new MensagemDTO(null, "comprador-1", "anunciante-1", "anuncio-1", "  Olá, aceita troca?  ", null);
            MensagemDTO dtoSalvo = new MensagemDTO("msg-1", "comprador-1", "anunciante-1", "anuncio-1", "Olá, aceita troca?", LocalDateTime.now());

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(comprador);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);
            when(conversaService.enviarMensagem(any(MensagemDTO.class))).thenReturn(dtoSalvo);

            // QUANDO
            ChatResponse<MensagemDTO> resposta = chatController.salvarMensagemAjax(dtoEnvio, sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals("sucesso", resposta.getMessage());
            assertEquals("Olá, aceita troca?", resposta.getData().conteudo());

            ArgumentCaptor<MensagemDTO> captor = ArgumentCaptor.forClass(MensagemDTO.class);
            verify(conversaService).enviarMensagem(captor.capture());
            assertEquals("comprador-1", captor.getValue().uidRemetente());
            assertEquals("anunciante-1", captor.getValue().uidDestinatario());
            assertEquals("Olá, aceita troca?", captor.getValue().conteudo());

            verify(interacaoService).registrarInicioConversa("comprador-1", anuncio.uidLivro(), anuncio.id());
        }

        @Test
        @DisplayName("CT01 - Deve salvar mensagem com sucesso quando anunciante responder, sem registrar nova interação")
        void deveSalvarMensagemComSucesso_quandoAnuncianteResponder_semRegistrarInteracao() {
            // DADO
            UsuarioOutput anunciante = criarUsuarioLogado("anunciante-1", "Dono");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "anunciante-1");
            MensagemDTO dtoEnvio = new MensagemDTO(null, "anunciante-1", "comprador-1", "anuncio-1", "Sim, aceito!", null);
            MensagemDTO dtoSalvo = new MensagemDTO("msg-2", "anunciante-1", "comprador-1", "anuncio-1", "Sim, aceito!", LocalDateTime.now());

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(anunciante);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);
            when(conversaService.enviarMensagem(any(MensagemDTO.class))).thenReturn(dtoSalvo);

            // QUANDO
            ChatResponse<MensagemDTO> resposta = chatController.salvarMensagemAjax(dtoEnvio, sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            verify(conversaService).enviarMensagem(any(MensagemDTO.class));
            verifyNoInteractions(interacaoService);
        }
    }

    // =========================================================================
    // 6. ATUALIZAR CONVERSA E CARREGAR CONVERSA (CT04, CT08, CT17)
    // =========================================================================
    @Nested
    @DisplayName("Testes de atualização e carregamento dos dados da conversa")
    class AtualizarECarregarConversaTests {

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não autenticado tentar atualizar conversa")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoAtualizarConversa() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.atualizarConversa("anuncio-1", "dest-1", sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(anuncioService);
            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT05 - Deve lançar IllegalArgumentException quando anúncio não existir ao atualizar conversa")
        void deveLancarIllegalArgumentException_quandoAnuncioNaoEncontradoAoAtualizarConversa() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Nome");
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(anuncioService.buscarPorUid("anuncio-invalido")).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.atualizarConversa("anuncio-invalido", "dest-1", sessao);
                fail("Deveria ter lançado IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertEquals("Anúncio não encontrado", e.getMessage());
            }

            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT08 - Deve atualizar conversa com sucesso quando usuário logado for o anunciante")
        void deveAtualizarConversaComSucesso_quandoUsuarioLogadoForAnunciante() {
            // DADO
            UsuarioOutput anunciante = criarUsuarioLogado("anunciante-1", "Vendedor");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "anunciante-1");
            MensagemDTO msg = new MensagemDTO("1", "comprador-1", "anunciante-1", "anuncio-1", "Proposta", LocalDateTime.now());
            NegociacaoDTO negociacao = criarNegociacaoExemplo("neg-1", "anunciante-1", "comprador-1", "anuncio-1", "TROCA");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(anunciante);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);
            when(conversaService.listarMensagens("anunciante-1", "comprador-1", anuncio)).thenReturn(List.of(msg));
            when(negociacaoService.buscarPorAnuncioEComprador("anuncio-1", "comprador-1")).thenReturn(negociacao);

            // QUANDO
            ChatResponse<ChatAtualizacaoResponse> resposta = chatController.atualizarConversa("anuncio-1", "comprador-1", sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals(1, resposta.getData().mensagens().size());
            assertEquals("neg-1", resposta.getData().negociacao().id());

            verify(conversaService).listarMensagens("anunciante-1", "comprador-1", anuncio);
            verify(negociacaoService).buscarPorAnuncioEComprador("anuncio-1", "comprador-1");
        }

        @Test
        @DisplayName("CT08 - Deve atualizar conversa com sucesso quando usuário logado for o comprador")
        void deveAtualizarConversaComSucesso_quandoUsuarioLogadoForComprador() {
            // DADO
            UsuarioOutput comprador = criarUsuarioLogado("comprador-1", "Comprador");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "anunciante-1");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(comprador);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);
            when(conversaService.listarMensagens("comprador-1", "anunciante-1", anuncio)).thenReturn(Collections.emptyList());
            when(negociacaoService.buscarPorAnuncioEComprador("anuncio-1", "comprador-1")).thenReturn(null);

            // QUANDO
            ChatResponse<ChatAtualizacaoResponse> resposta = chatController.atualizarConversa("anuncio-1", "anunciante-1", sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals(true, resposta.getData().mensagens().isEmpty());
            assertNull(resposta.getData().negociacao());

            verify(negociacaoService).buscarPorAnuncioEComprador("anuncio-1", "comprador-1");
        }

        @Test
        @DisplayName("CT04 - Deve carregar dados da conversa com sucesso quando destinatário for o dono do anúncio")
        void deveCarregarConversaComSucesso_quandoDestinatarioForDonoDoAnuncio() {
            // DADO
            UsuarioOutput comprador = criarUsuarioLogado("comprador-1", "Comprador");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "anunciante-1");
            MensagemDTO msg = new MensagemDTO("1", "comprador-1", "anunciante-1", "anuncio-1", "Olá!", LocalDateTime.now());

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(comprador);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);
            when(conversaService.listarMensagens("comprador-1", "anunciante-1", anuncio)).thenReturn(List.of(msg));
            when(negociacaoService.buscarPorAnuncioEComprador("anuncio-1", "comprador-1")).thenReturn(null);

            // QUANDO
            ChatResponse<ChatConversaResponse> resposta = chatController.carregarConversa("anuncio-1", "anunciante-1", sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            ChatConversaResponse dados = resposta.getData();
            assertEquals("anuncio-1", dados.anuncio().id());
            assertEquals("anunciante-1", dados.usuarioNegociante().id());
            assertEquals(anuncio.nomeUsuario(), dados.usuarioNegociante().nome());
            assertEquals(1, dados.mensagens().size());

            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("CT04 - Deve carregar dados da conversa com sucesso buscando perfil do comprador quando o anunciante acessar")
        void deveCarregarConversaComSucesso_quandoDestinatarioForComprador() {
            // DADO
            UsuarioOutput anunciante = criarUsuarioLogado("anunciante-1", "Vendedor");
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-1", "anunciante-1");
            UsuarioOutput comprador = new UsuarioOutput("comprador-1", "Comprador Silva", "http://foto-comprador.jpg");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(anunciante);
            when(anuncioService.buscarPorUid("anuncio-1")).thenReturn(anuncio);
            when(conversaService.listarMensagens("anunciante-1", "comprador-1", anuncio)).thenReturn(Collections.emptyList());
            when(usuarioService.buscarPorUid("comprador-1")).thenReturn(comprador);
            when(negociacaoService.buscarPorAnuncioEComprador("anuncio-1", "comprador-1")).thenReturn(null);

            // QUANDO
            ChatResponse<ChatConversaResponse> resposta = chatController.carregarConversa("anuncio-1", "comprador-1", sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals("comprador-1", resposta.getData().usuarioNegociante().id());
            assertEquals("Comprador Silva", resposta.getData().usuarioNegociante().nome());

            verify(usuarioService).buscarPorUid("comprador-1");
        }
    }

    // =========================================================================
    // 7. ATUALIZAR CONVERSAS E ANÚNCIOS OFERECÍVEIS (CT07, CT17)
    // =========================================================================
    @Nested
    @DisplayName("Testes de atualização da lista de conversas e listagem de anúncios oferecíveis")
    class AtualizarConversasEAnunciosOfereciveisTests {

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não autenticado tentar atualizar lista de conversas")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoAtualizarConversas() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.atualizarConversas(sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(conversaService);
        }

        @Test
        @DisplayName("CT07 - Deve atualizar lista de conversas com sucesso para usuário autenticado")
        void deveAtualizarConversasComSucesso_quandoUsuarioEstiverAutenticado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Pedro");
            ConversaDTO conversa = new ConversaDTO();
            conversa.setUidAnuncio("anuncio-1");
            conversa.setUltimaMensagem("Última interação");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(conversaService.listarConversas("user-1")).thenReturn(List.of(conversa));

            // QUANDO
            ChatResponse<List<ConversaDTO>> resposta = chatController.atualizarConversas(sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals(1, resposta.getData().size());
            assertEquals("anuncio-1", resposta.getData().get(0).getUidAnuncio());

            verify(conversaService).listarConversas("user-1");
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não autenticado tentar listar anúncios oferecíveis")
        void deveLancarSecurityException_quandoUsuarioNaoEstiverAutenticadoAoListarAnunciosOfereciveis() {
            // DADO
            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(null);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                chatController.listarAnunciosOfereciveis(sessao);
                fail("Deveria ter lançado SecurityException");
            } catch (SecurityException e) {
                assertEquals("Usuário não autenticado", e.getMessage());
            }

            verifyNoInteractions(anuncioService);
        }

        @Test
        @DisplayName("Deve listar anúncios oferecíveis com sucesso para o usuário autenticado")
        void deveListarAnunciosOfereciveisComSucesso_quandoUsuarioEstiverAutenticado() {
            // DADO
            UsuarioOutput usuarioLogado = criarUsuarioLogado("user-1", "Pedro");
            AnuncioDTO anuncioTrocavel = criarAnuncioExemplo("anuncio-troca", "user-1");

            when(usuarioAutenticadoService.getUsuarioOutput(sessao)).thenReturn(usuarioLogado);
            when(anuncioService.listarAnunciosAtivosTrocaveis("user-1")).thenReturn(List.of(anuncioTrocavel));

            // QUANDO
            ChatResponse<List<AnuncioDTO>> resposta = chatController.listarAnunciosOfereciveis(sessao);

            // ENTÃO
            assertNotNull(resposta);
            assertEquals("success", resposta.getStatus());
            assertEquals(1, resposta.getData().size());
            assertEquals("anuncio-troca", resposta.getData().get(0).id());

            verify(anuncioService).listarAnunciosAtivosTrocaveis("user-1");
        }
    }
}

