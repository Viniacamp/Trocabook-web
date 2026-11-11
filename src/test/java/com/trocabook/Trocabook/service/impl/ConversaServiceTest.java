package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.controllers.response.ChatResponse;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.AtualizarMensagemDTO;
import com.trocabook.Trocabook.model.dto.ConversaDTO;
import com.trocabook.Trocabook.model.dto.MensagemDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.feign.ChatRenderClient;
import com.trocabook.Trocabook.service.feign.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Suíte de testes unitários para a classe {@link ConversaService}.
 * Atende às especificações do relatório técnico (REQ10 - Chat de Alto Risco, CT01 a CT18).
 * Padrão BDD (// DADO, // QUANDO, // ENTÃO) com tratamento defensivo de exceções (sem assertThrows)
 * e validação exaustiva de mecanismos de resiliência/fallback (Azure -> Render).
 */
@ExtendWith(MockitoExtension.class)
class ConversaServiceTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatRenderClient chatRenderClient;

    @Mock
    private IUsuarioService usuarioService;

    @InjectMocks
    private ConversaService conversaService;

    private AnuncioDTO criarAnuncioExemplo(String id, String uidUsuario) {
        return new AnuncioDTO(
                id,
                uidUsuario,
                "livro-100",
                "Usuario Teste",
                "TROCA",
                "Dom Casmurro",
                "http://foto.jpg",
                "http://capa.jpg",
                List.of("Machado de Assis"),
                List.of("Literatura Brasileira"),
                "Livro em ótimo estado",
                "ATIVO"
        );
    }

    // =========================================================================
    // 1. LISTAR CONVERSAS (CT07, CT09 e Resiliência)
    // =========================================================================
    @Nested
    @DisplayName("Testes do método listarConversas")
    class ListarConversasTests {

        @Test
        @DisplayName("CT07 - Deve listar conversas com sucesso quando Azure responder normalmente")
        void deveListarConversasComSucesso_quandoAzureResponderNormalmente() {
            // DADO
            String uidUsuarioLogado = "user-logado";
            MensagemDTO msg1 = new MensagemDTO(
                    "msg-1",
                    uidUsuarioLogado,
                    "dest-1",
                    "anuncio-1",
                    "Olá, tenho interesse!",
                    LocalDateTime.now()
            );
            MensagemDTO msg2 = new MensagemDTO(
                    "msg-2",
                    "remet-2",
                    uidUsuarioLogado,
                    "anuncio-2",
                    "Ainda está disponível?",
                    LocalDateTime.now()
            );
            ChatResponse<List<MensagemDTO>> respostaAzure = new ChatResponse<>(List.of(msg1, msg2), "sucesso");

            UsuarioOutput userDest1 = new UsuarioOutput("dest-1", "Carlos Silva", "http://foto1.jpg");
            UsuarioOutput userRemet2 = new UsuarioOutput("remet-2", "Mariana Souza", "http://foto2.jpg");

            when(chatService.listarMensagens(uidUsuarioLogado, null, null)).thenReturn(respostaAzure);
            when(usuarioService.buscarPorUid("dest-1")).thenReturn(userDest1);
            when(usuarioService.buscarPorUid("remet-2")).thenReturn(userRemet2);

            // QUANDO
            List<ConversaDTO> conversas = conversaService.listarConversas(uidUsuarioLogado);

            // ENTÃO
            assertNotNull(conversas);
            assertEquals(2, conversas.size());

            ConversaDTO conversa1 = conversas.get(0);
            assertEquals("anuncio-1", conversa1.getUidAnuncio());
            assertEquals("dest-1", conversa1.getUidDestinatario());
            assertEquals("Carlos Silva", conversa1.getNomeDestinatario());
            assertEquals("http://foto1.jpg", conversa1.getFotoDestinatario());
            assertEquals("Olá, tenho interesse!", conversa1.getUltimaMensagem());
            assertTrue(conversa1.isEnviadaPeloUsuarioLogado());

            ConversaDTO conversa2 = conversas.get(1);
            assertEquals("anuncio-2", conversa2.getUidAnuncio());
            assertEquals("remet-2", conversa2.getUidDestinatario());
            assertEquals("Mariana Souza", conversa2.getNomeDestinatario());
            assertEquals("http://foto2.jpg", conversa2.getFotoDestinatario());
            assertEquals("Ainda está disponível?", conversa2.getUltimaMensagem());
            assertFalse(conversa2.isEnviadaPeloUsuarioLogado());

            verify(chatService).listarMensagens(uidUsuarioLogado, null, null);
            verifyNoInteractions(chatRenderClient);
            verify(usuarioService).buscarPorUid("dest-1");
            verify(usuarioService).buscarPorUid("remet-2");
        }

        @Test
        @DisplayName("CT07 - Deve filtrar conversa quando o usuário destinatário não for encontrado")
        void deveFiltrarConversa_quandoDestinatarioNaoForEncontrado() {
            // DADO
            String uidUsuarioLogado = "user-logado";
            MensagemDTO msg = new MensagemDTO(
                    "msg-1",
                    uidUsuarioLogado,
                    "dest-fantasma",
                    "anuncio-1",
                    "Mensagem para usuário inexistente",
                    LocalDateTime.now()
            );
            ChatResponse<List<MensagemDTO>> respostaAzure = new ChatResponse<>(List.of(msg), "sucesso");

            when(chatService.listarMensagens(uidUsuarioLogado, null, null)).thenReturn(respostaAzure);
            when(usuarioService.buscarPorUid("dest-fantasma")).thenReturn(null);

            // QUANDO
            List<ConversaDTO> conversas = conversaService.listarConversas(uidUsuarioLogado);

            // ENTÃO
            assertNotNull(conversas);
            assertTrue(conversas.isEmpty());

            verify(chatService).listarMensagens(uidUsuarioLogado, null, null);
            verifyNoInteractions(chatRenderClient);
            verify(usuarioService).buscarPorUid("dest-fantasma");
        }

        @Test
        @DisplayName("CT07 / Resiliência - Deve acionar com sucesso o fallback para Render quando Azure falhar ao listar conversas")
        void deveAcionarFallbackRenderComSucesso_quandoAzureFalharAoListarConversas() {
            // DADO
            String uidUsuarioLogado = "user-logado";
            MensagemDTO msg = new MensagemDTO(
                    "msg-render",
                    "user-outro",
                    uidUsuarioLogado,
                    "anuncio-render",
                    "Recuperado pelo Render",
                    LocalDateTime.now()
            );
            ChatResponse<List<MensagemDTO>> respostaRender = new ChatResponse<>(List.of(msg), "sucesso");
            UsuarioOutput destinatario = new UsuarioOutput("user-outro", "Ana Paula", "http://foto-ana.jpg");

            when(chatService.listarMensagens(uidUsuarioLogado, null, null))
                    .thenThrow(new RuntimeException("Falha de conexão com Azure"));
            when(chatRenderClient.listarMensagens(uidUsuarioLogado, null, null))
                    .thenReturn(respostaRender);
            when(usuarioService.buscarPorUid("user-outro")).thenReturn(destinatario);

            // QUANDO
            List<ConversaDTO> conversas = conversaService.listarConversas(uidUsuarioLogado);

            // ENTÃO
            assertNotNull(conversas);
            assertEquals(1, conversas.size());
            assertEquals("anuncio-render", conversas.get(0).getUidAnuncio());
            assertEquals("Ana Paula", conversas.get(0).getNomeDestinatario());
            assertEquals("Recuperado pelo Render", conversas.get(0).getUltimaMensagem());
            assertFalse(conversas.get(0).isEnviadaPeloUsuarioLogado());

            verify(chatService).listarMensagens(uidUsuarioLogado, null, null);
            verify(chatRenderClient).listarMensagens(uidUsuarioLogado, null, null);
            verify(usuarioService).buscarPorUid("user-outro");
        }

        @Test
        @DisplayName("CT09 / Resiliência - Deve retornar lista vazia quando ambas as instâncias (Azure e Render) falharem")
        void deveRetornarListaVazia_quandoAmbasAsInstanciasFalharemAoListarConversas() {
            // DADO
            String uidUsuarioLogado = "user-logado";
            when(chatService.listarMensagens(uidUsuarioLogado, null, null))
                    .thenThrow(new RuntimeException("Timeout no Azure"));
            when(chatRenderClient.listarMensagens(uidUsuarioLogado, null, null))
                    .thenThrow(new RuntimeException("Erro 500 no Render"));

            // QUANDO
            List<ConversaDTO> conversas = conversaService.listarConversas(uidUsuarioLogado);

            // ENTÃO
            assertNotNull(conversas);
            assertTrue(conversas.isEmpty());

            verify(chatService).listarMensagens(uidUsuarioLogado, null, null);
            verify(chatRenderClient).listarMensagens(uidUsuarioLogado, null, null);
            verifyNoInteractions(usuarioService);
        }

        @Test
        @DisplayName("CT07 - Deve retornar lista vazia quando resposta ou data forem nulos")
        void deveRetornarListaVazia_quandoRespostaOuDataForemNulos() {
            // DADO
            String uidUsuarioLogado = "user-logado";
            when(chatService.listarMensagens(uidUsuarioLogado, null, null))
                    .thenReturn(new ChatResponse<>(null, "sem dados"));

            // QUANDO
            List<ConversaDTO> conversas = conversaService.listarConversas(uidUsuarioLogado);

            // ENTÃO
            assertNotNull(conversas);
            assertTrue(conversas.isEmpty());

            verify(chatService).listarMensagens(uidUsuarioLogado, null, null);
            verifyNoInteractions(chatRenderClient);
            verifyNoInteractions(usuarioService);
        }
    }

    // =========================================================================
    // 2. LISTAR MENSAGENS (CT08, CT09, CT17 e Resiliência)
    // =========================================================================
    @Nested
    @DisplayName("Testes do método listarMensagens")
    class ListarMensagensTests {

        @Test
        @DisplayName("CT08 - Deve listar mensagens com sucesso quando o usuário for o comprador e Azure responder")
        void deveListarMensagensComSucesso_quandoUsuarioCompradorAcessarConversa() {
            // DADO
            String uidComprador = "comprador-1";
            String uidAnunciante = "anunciante-1";
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", uidAnunciante);

            MensagemDTO m1 = new MensagemDTO("1", uidComprador, uidAnunciante, anuncio.id(), "Olá!", LocalDateTime.now());
            MensagemDTO m2 = new MensagemDTO("2", uidAnunciante, uidComprador, anuncio.id(), "Olá, tudo bem?", LocalDateTime.now());
            ChatResponse<List<MensagemDTO>> respostaAzure = new ChatResponse<>(List.of(m1, m2), "sucesso");

            when(chatService.listarMensagens(uidComprador, uidAnunciante, anuncio.id()))
                    .thenReturn(respostaAzure);

            // QUANDO
            List<MensagemDTO> resultado = conversaService.listarMensagens(uidComprador, uidAnunciante, anuncio);

            // ENTÃO
            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals("Olá!", resultado.get(0).conteudo());
            assertEquals("Olá, tudo bem?", resultado.get(1).conteudo());

            verify(chatService).listarMensagens(uidComprador, uidAnunciante, anuncio.id());
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT08 - Deve listar mensagens com sucesso quando o anunciante acessar conversa com mensagens existentes")
        void deveListarMensagensComSucesso_quandoAnuncianteAcessarConversaComMensagens() {
            // DADO
            String uidAnunciante = "anunciante-1";
            String uidComprador = "comprador-1";
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", uidAnunciante);

            MensagemDTO m = new MensagemDTO("1", uidComprador, uidAnunciante, anuncio.id(), "Mensagem inicial", LocalDateTime.now());
            ChatResponse<List<MensagemDTO>> respostaAzure = new ChatResponse<>(List.of(m), "sucesso");

            when(chatService.listarMensagens(uidAnunciante, uidComprador, anuncio.id()))
                    .thenReturn(respostaAzure);

            // QUANDO
            List<MensagemDTO> resultado = conversaService.listarMensagens(uidAnunciante, uidComprador, anuncio);

            // ENTÃO
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("Mensagem inicial", resultado.get(0).conteudo());

            verify(chatService).listarMensagens(uidAnunciante, uidComprador, anuncio.id());
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT08 / Resiliência - Deve acionar com sucesso o fallback Render quando Azure falhar ao listar mensagens")
        void deveAcionarFallbackRenderComSucesso_quandoAzureFalharAoListarMensagens() {
            // DADO
            String uidComprador = "comprador-1";
            String uidAnunciante = "anunciante-1";
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", uidAnunciante);

            MensagemDTO m = new MensagemDTO("10", uidComprador, uidAnunciante, anuncio.id(), "Mensagem Render", LocalDateTime.now());
            ChatResponse<List<MensagemDTO>> respostaRender = new ChatResponse<>(List.of(m), "sucesso");

            when(chatService.listarMensagens(uidComprador, uidAnunciante, anuncio.id()))
                    .thenThrow(new RuntimeException("Azure indisponível"));
            when(chatRenderClient.listarMensagens(uidComprador, uidAnunciante, anuncio.id()))
                    .thenReturn(respostaRender);

            // QUANDO
            List<MensagemDTO> resultado = conversaService.listarMensagens(uidComprador, uidAnunciante, anuncio);

            // ENTÃO
            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("Mensagem Render", resultado.get(0).conteudo());

            verify(chatService).listarMensagens(uidComprador, uidAnunciante, anuncio.id());
            verify(chatRenderClient).listarMensagens(uidComprador, uidAnunciante, anuncio.id());
        }

        @Test
        @DisplayName("CT09 / Resiliência - Deve retornar lista vazia quando ambas as instâncias falharem e o usuário for comprador")
        void deveRetornarListaVazia_quandoAmbasAsInstanciasFalharemEUsuarioForComprador() {
            // DADO
            String uidComprador = "comprador-1";
            String uidAnunciante = "anunciante-1";
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", uidAnunciante);

            when(chatService.listarMensagens(uidComprador, uidAnunciante, anuncio.id()))
                    .thenThrow(new RuntimeException("Azure Down"));
            when(chatRenderClient.listarMensagens(uidComprador, uidAnunciante, anuncio.id()))
                    .thenThrow(new RuntimeException("Render Down"));

            // QUANDO
            List<MensagemDTO> resultado = conversaService.listarMensagens(uidComprador, uidAnunciante, anuncio);

            // ENTÃO
            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());

            verify(chatService).listarMensagens(uidComprador, uidAnunciante, anuncio.id());
            verify(chatRenderClient).listarMensagens(uidComprador, uidAnunciante, anuncio.id());
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando o anunciante tentar acessar conversa sem mensagens")
        void deveLancarSecurityException_quandoAnuncianteAcessarConversaSemMensagens() {
            // DADO
            String uidAnunciante = "anunciante-1";
            String uidComprador = "comprador-1";
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", uidAnunciante);

            ChatResponse<List<MensagemDTO>> respostaVazia = new ChatResponse<>(Collections.emptyList(), "sem mensagens");
            when(chatService.listarMensagens(uidAnunciante, uidComprador, anuncio.id()))
                    .thenReturn(respostaVazia);

            // QUANDO / ENTÃO (Tratamento defensivo sem assertThrows)
            try {
                conversaService.listarMensagens(uidAnunciante, uidComprador, anuncio);
                fail("Deveria ter lançado SecurityException quando o anunciante não possuir mensagens na conversa");
            } catch (SecurityException e) {
                assertEquals("Você não possui acesso a esta conversa", e.getMessage());
            }

            verify(chatService).listarMensagens(uidAnunciante, uidComprador, anuncio.id());
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando ambas as instâncias falharem e o usuário for anunciante")
        void deveLancarSecurityException_quandoAmbasFalharemEUsuarioForAnunciante() {
            // DADO
            String uidAnunciante = "anunciante-1";
            String uidComprador = "comprador-1";
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", uidAnunciante);

            when(chatService.listarMensagens(uidAnunciante, uidComprador, anuncio.id()))
                    .thenThrow(new RuntimeException("Azure Down"));
            when(chatRenderClient.listarMensagens(uidAnunciante, uidComprador, anuncio.id()))
                    .thenThrow(new RuntimeException("Render Down"));

            // QUANDO / ENTÃO (Tratamento defensivo sem assertThrows)
            try {
                conversaService.listarMensagens(uidAnunciante, uidComprador, anuncio);
                fail("Deveria ter lançado SecurityException para anunciante quando ambas nuvens retornarem lista vazia");
            } catch (SecurityException e) {
                assertEquals("Você não possui acesso a esta conversa", e.getMessage());
            }

            verify(chatService).listarMensagens(uidAnunciante, uidComprador, anuncio.id());
            verify(chatRenderClient).listarMensagens(uidAnunciante, uidComprador, anuncio.id());
        }

        @Test
        @DisplayName("CT08 - Deve lançar IllegalArgumentException quando o destinatário for nulo ou em branco")
        void deveLancarIllegalArgumentException_quandoDestinatarioForNuloOuVazio() {
            // DADO
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", "anunciante-1");

            // QUANDO / ENTÃO - Destinatário nulo
            try {
                conversaService.listarMensagens("user-1", null, anuncio);
                fail("Deveria ter lançado IllegalArgumentException para destinatário nulo");
            } catch (IllegalArgumentException e) {
                assertEquals("Destinatário não informado", e.getMessage());
            }

            // QUANDO / ENTÃO - Destinatário em branco
            try {
                conversaService.listarMensagens("user-1", "   ", anuncio);
                fail("Deveria ter lançado IllegalArgumentException para destinatário em branco");
            } catch (IllegalArgumentException e) {
                assertEquals("Destinatário não informado", e.getMessage());
            }

            verifyNoInteractions(chatService);
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT08 - Deve lançar IllegalArgumentException quando o usuário tentar conversar consigo mesmo")
        void deveLancarIllegalArgumentException_quandoUsuarioTentarConversarConsigoMesmo() {
            // DADO
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", "user-1");

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.listarMensagens("user-1", "user-1", anuncio);
                fail("Deveria ter lançado IllegalArgumentException quando usuário conversa consigo mesmo");
            } catch (IllegalArgumentException e) {
                assertEquals("Não é possível acessar uma conversa consigo mesmo", e.getMessage());
            }

            verifyNoInteractions(chatService);
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário logado e destinatário não forem o anunciante")
        void deveLancarSecurityException_quandoUsuarioNaoTiverAcessoAoAnuncio() {
            // DADO
            AnuncioDTO anuncio = criarAnuncioExemplo("anuncio-10", "dono-do-anuncio");

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.listarMensagens("intruso-1", "intruso-2", anuncio);
                fail("Deveria ter lançado SecurityException para usuário sem acesso ao anúncio");
            } catch (SecurityException e) {
                assertEquals("Você não possui acesso a esta conversa", e.getMessage());
            }

            verifyNoInteractions(chatService);
            verifyNoInteractions(chatRenderClient);
        }
    }

    // =========================================================================
    // 3. ENVIAR MENSAGEM (CT01, CT03 e Resiliência)
    // =========================================================================
    @Nested
    @DisplayName("Testes do método enviarMensagem")
    class EnviarMensagemTests {

        @Test
        @DisplayName("CT01 - Deve enviar mensagem com sucesso pelo Azure")
        void deveEnviarMensagemComSucesso_peloAzure() {
            // DADO
            MensagemDTO dtoEnvio = new MensagemDTO(
                    null,
                    "remet-1",
                    "dest-1",
                    "anuncio-1",
                    "Olá, gostaria de trocar!",
                    null
            );
            MensagemDTO dtoSalvo = new MensagemDTO(
                    "msg-100",
                    "remet-1",
                    "dest-1",
                    "anuncio-1",
                    "Olá, gostaria de trocar!",
                    LocalDateTime.now()
            );
            ChatResponse<MensagemDTO> respostaAzure = new ChatResponse<>(dtoSalvo, "sucesso");

            when(chatService.enviarMensagem(dtoEnvio)).thenReturn(respostaAzure);

            // QUANDO
            MensagemDTO resultado = conversaService.enviarMensagem(dtoEnvio);

            // ENTÃO
            assertNotNull(resultado);
            assertEquals("msg-100", resultado.id());
            assertEquals("Olá, gostaria de trocar!", resultado.conteudo());

            verify(chatService).enviarMensagem(dtoEnvio);
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT01 / Resiliência - Deve acionar com sucesso o fallback Render quando Azure falhar ao enviar mensagem")
        void deveAcionarFallbackRenderComSucesso_quandoAzureFalharAoEnviarMensagem() {
            // DADO
            MensagemDTO dtoEnvio = new MensagemDTO(
                    null,
                    "remet-1",
                    "dest-1",
                    "anuncio-1",
                    "Tentativa com fallback",
                    null
            );
            MensagemDTO dtoSalvoRender = new MensagemDTO(
                    "msg-render-1",
                    "remet-1",
                    "dest-1",
                    "anuncio-1",
                    "Tentativa com fallback",
                    LocalDateTime.now()
            );
            ChatResponse<MensagemDTO> respostaRender = new ChatResponse<>(dtoSalvoRender, "sucesso");

            when(chatService.enviarMensagem(dtoEnvio))
                    .thenThrow(new RuntimeException("Azure fora do ar"));
            when(chatRenderClient.enviarMensagem(dtoEnvio))
                    .thenReturn(respostaRender);

            // QUANDO
            MensagemDTO resultado = conversaService.enviarMensagem(dtoEnvio);

            // ENTÃO
            assertNotNull(resultado);
            assertEquals("msg-render-1", resultado.id());
            assertEquals("Tentativa com fallback", resultado.conteudo());

            verify(chatService).enviarMensagem(dtoEnvio);
            verify(chatRenderClient).enviarMensagem(dtoEnvio);
        }

        @Test
        @DisplayName("CT03 / Resiliência - Deve lançar exceção quando ambas as instâncias (Azure e Render) falharem ao enviar")
        void deveLancarExcecao_quandoAmbasAsInstanciasFalharemAoEnviarMensagem() {
            // DADO
            MensagemDTO dtoEnvio = new MensagemDTO(
                    null,
                    "remet-1",
                    "dest-1",
                    "anuncio-1",
                    "Mensagem perdida",
                    null
            );

            when(chatService.enviarMensagem(dtoEnvio))
                    .thenThrow(new RuntimeException("Falha no Azure"));
            when(chatRenderClient.enviarMensagem(dtoEnvio))
                    .thenThrow(new RuntimeException("Render também indisponível"));

            // QUANDO / ENTÃO (Tratamento defensivo sem assertThrows)
            try {
                conversaService.enviarMensagem(dtoEnvio);
                fail("Deveria ter lançado exceção quando ambas as nuvens falharem no envio");
            } catch (RuntimeException e) {
                assertEquals("Render também indisponível", e.getMessage());
            }

            verify(chatService).enviarMensagem(dtoEnvio);
            verify(chatRenderClient).enviarMensagem(dtoEnvio);
        }
    }

    // =========================================================================
    // 4. ATUALIZAR MENSAGEM (CT10, CT12, CT13, CT17 e Resiliência)
    // =========================================================================
    @Nested
    @DisplayName("Testes do método atualizarMensagem")
    class AtualizarMensagemTests {

        @Test
        @DisplayName("CT10 - Deve atualizar mensagem com sucesso pelo Azure")
        void deveAtualizarMensagemComSucesso_peloAzure() {
            // DADO
            String idMensagem = "msg-1";
            String uidRemetente = "user-autor";
            AtualizarMensagemDTO dtoRequisicao = new AtualizarMensagemDTO("Texto modificado");

            MensagemDTO mensagemOriginal = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Texto original",
                    LocalDateTime.now()
            );
            MensagemDTO mensagemAtualizada = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Texto modificado",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagemOriginal, "sucesso"));
            when(chatService.alterarMensagem(eq(idMensagem), eq(new AtualizarMensagemDTO("Texto modificado"))))
                    .thenReturn(new ChatResponse<>(mensagemAtualizada, "sucesso"));

            // QUANDO
            MensagemDTO resultado = conversaService.atualizarMensagem(idMensagem, uidRemetente, dtoRequisicao);

            // ENTÃO
            assertNotNull(resultado);
            assertEquals("Texto modificado", resultado.conteudo());

            verify(chatService).buscarMensagemPorId(idMensagem);
            verify(chatService).alterarMensagem(eq(idMensagem), eq(new AtualizarMensagemDTO("Texto modificado")));
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT10 / Resiliência - Deve acionar com sucesso o fallback Render na busca e na alteração")
        void deveAcionarFallbackRenderComSucesso_quandoAzureFalharNaBuscaOuNaAlteracao() {
            // DADO
            String idMensagem = "msg-1";
            String uidRemetente = "user-autor";
            AtualizarMensagemDTO dtoRequisicao = new AtualizarMensagemDTO("  Texto alterado via Render  ");

            MensagemDTO mensagemOriginal = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Texto antigo",
                    LocalDateTime.now()
            );
            MensagemDTO mensagemSalvaRender = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Texto alterado via Render",
                    LocalDateTime.now()
            );

            // Azure falha na busca -> Render responde
            when(chatService.buscarMensagemPorId(idMensagem)).thenThrow(new RuntimeException("Azure Down na busca"));
            when(chatRenderClient.buscarMensagemPorId(idMensagem)).thenReturn(new ChatResponse<>(mensagemOriginal, "sucesso"));

            // Azure falha na alteração -> Render responde
            when(chatService.alterarMensagem(eq(idMensagem), any())).thenThrow(new RuntimeException("Azure Down na alteração"));
            when(chatRenderClient.alterarMensagem(eq(idMensagem), any())).thenReturn(new ChatResponse<>(mensagemSalvaRender, "sucesso"));

            // QUANDO
            MensagemDTO resultado = conversaService.atualizarMensagem(idMensagem, uidRemetente, dtoRequisicao);

            // ENTÃO
            assertNotNull(resultado);
            assertEquals("Texto alterado via Render", resultado.conteudo());

            verify(chatService).buscarMensagemPorId(idMensagem);
            verify(chatRenderClient).buscarMensagemPorId(idMensagem);
            verify(chatService).alterarMensagem(eq(idMensagem), eq(new AtualizarMensagemDTO("Texto alterado via Render")));
            verify(chatRenderClient).alterarMensagem(eq(idMensagem), eq(new AtualizarMensagemDTO("Texto alterado via Render")));
        }

        @Test
        @DisplayName("CT12 - Deve lançar IllegalArgumentException quando mensagem não for encontrada para atualizar")
        void deveLancarIllegalArgumentException_quandoMensagemNaoForEncontradaAoAtualizar() {
            // DADO
            String idMensagem = "msg-inexistente";
            when(chatService.buscarMensagemPorId(idMensagem)).thenReturn(new ChatResponse<>(null, "não encontrada"));

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.atualizarMensagem(idMensagem, "user-1", new AtualizarMensagemDTO("Novo"));
                fail("Deveria ter lançado IllegalArgumentException para mensagem inexistente");
            } catch (IllegalArgumentException e) {
                assertEquals("Mensagem não encontrada", e.getMessage());
            }

            verify(chatService).buscarMensagemPorId(idMensagem);
            verify(chatService, never()).alterarMensagem(any(), any());
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não for o remetente ao tentar atualizar mensagem")
        void deveLancarSecurityException_quandoUsuarioNaoForRemetenteAoAtualizar() {
            // DADO
            String idMensagem = "msg-1";
            MensagemDTO mensagemOriginal = new MensagemDTO(
                    idMensagem,
                    "remetente-original",
                    "dest-1",
                    "anuncio-1",
                    "Texto",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagemOriginal, "sucesso"));

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.atualizarMensagem(idMensagem, "usuario-intruso", new AtualizarMensagemDTO("Hackeado"));
                fail("Deveria ter lançado SecurityException para usuário não autor");
            } catch (SecurityException e) {
                assertEquals("Você não possui permissão para alterar esta mensagem", e.getMessage());
            }

            verify(chatService).buscarMensagemPorId(idMensagem);
            verify(chatService, never()).alterarMensagem(any(), any());
        }

        @Test
        @DisplayName("CT10 - Deve lançar IllegalArgumentException quando conteúdo for nulo ou em branco")
        void deveLancarIllegalArgumentException_quandoConteudoForNuloOuVazioAoAtualizar() {
            // DADO
            String idMensagem = "msg-1";
            String uidRemetente = "user-autor";
            MensagemDTO mensagemOriginal = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Texto",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagemOriginal, "sucesso"));

            // QUANDO / ENTÃO - Conteúdo nulo
            try {
                conversaService.atualizarMensagem(idMensagem, uidRemetente, new AtualizarMensagemDTO(null));
                fail("Deveria ter lançado IllegalArgumentException para conteúdo nulo");
            } catch (IllegalArgumentException e) {
                assertEquals("A mensagem não pode estar vazia", e.getMessage());
            }

            // QUANDO / ENTÃO - Conteúdo em branco
            try {
                conversaService.atualizarMensagem(idMensagem, uidRemetente, new AtualizarMensagemDTO("   "));
                fail("Deveria ter lançado IllegalArgumentException para conteúdo em branco");
            } catch (IllegalArgumentException e) {
                assertEquals("A mensagem não pode estar vazia", e.getMessage());
            }

            verify(chatService, never()).alterarMensagem(any(), any());
        }

        @Test
        @DisplayName("CT13 / Resiliência - Deve lançar exceção quando ambas as instâncias falharem na alteração")
        void deveLancarExcecao_quandoAmbasAsInstanciasFalharemAoAlterarMensagem() {
            // DADO
            String idMensagem = "msg-1";
            String uidRemetente = "user-autor";
            AtualizarMensagemDTO dto = new AtualizarMensagemDTO("Texto valido");

            MensagemDTO mensagemOriginal = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Texto original",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagemOriginal, "sucesso"));
            when(chatService.alterarMensagem(eq(idMensagem), any()))
                    .thenThrow(new RuntimeException("Azure falhou"));
            when(chatRenderClient.alterarMensagem(eq(idMensagem), any()))
                    .thenThrow(new RuntimeException("Render falhou"));

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.atualizarMensagem(idMensagem, uidRemetente, dto);
                fail("Deveria ter lançado exceção quando ambas as nuvens falharem na alteração");
            } catch (RuntimeException e) {
                assertEquals("Render falhou", e.getMessage());
            }

            verify(chatService).alterarMensagem(eq(idMensagem), any());
            verify(chatRenderClient).alterarMensagem(eq(idMensagem), any());
        }
    }

    // =========================================================================
    // 5. EXCLUIR MENSAGEM (CT14, CT16, CT17, CT18 e Resiliência)
    // =========================================================================
    @Nested
    @DisplayName("Testes do método excluirMensagem")
    class ExcluirMensagemTests {

        @Test
        @DisplayName("CT14 - Deve excluir mensagem com sucesso pelo Azure")
        void deveExcluirMensagemComSucesso_peloAzure() {
            // DADO
            String idMensagem = "msg-1";
            String uidRemetente = "user-autor";
            MensagemDTO mensagem = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Mensagem para apagar",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagem, "sucesso"));

            // QUANDO
            conversaService.excluirMensagem(idMensagem, uidRemetente);

            // ENTÃO
            verify(chatService).buscarMensagemPorId(idMensagem);
            verify(chatService).excluirMensagem(idMensagem);
            verifyNoInteractions(chatRenderClient);
        }

        @Test
        @DisplayName("CT14 / Resiliência - Deve acionar com sucesso o fallback Render na exclusão quando Azure falhar")
        void deveAcionarFallbackRenderComSucesso_quandoAzureFalharNaExclusao() {
            // DADO
            String idMensagem = "msg-1";
            String uidRemetente = "user-autor";
            MensagemDTO mensagem = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Mensagem para apagar",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagem, "sucesso"));
            doThrow(new RuntimeException("Falha na exclusão do Azure"))
                    .when(chatService).excluirMensagem(idMensagem);

            // QUANDO
            conversaService.excluirMensagem(idMensagem, uidRemetente);

            // ENTÃO
            verify(chatService).excluirMensagem(idMensagem);
            verify(chatRenderClient).excluirMensagem(idMensagem);
        }

        @Test
        @DisplayName("CT16 - Deve lançar IllegalArgumentException quando mensagem não for encontrada para exclusão")
        void deveLancarIllegalArgumentException_quandoMensagemNaoForEncontradaAoExcluir() {
            // DADO
            String idMensagem = "msg-inexistente";
            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(null, "não encontrada"));

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.excluirMensagem(idMensagem, "user-1");
                fail("Deveria ter lançado IllegalArgumentException quando mensagem não for encontrada para exclusão");
            } catch (IllegalArgumentException e) {
                assertEquals("Mensagem não encontrada", e.getMessage());
            }

            verify(chatService).buscarMensagemPorId(idMensagem);
            verify(chatService, never()).excluirMensagem(any());
            verify(chatRenderClient, never()).excluirMensagem(any());
        }

        @Test
        @DisplayName("CT17 - Deve lançar SecurityException quando usuário não for o remetente ao tentar excluir")
        void deveLancarSecurityException_quandoUsuarioNaoForRemetenteAoExcluir() {
            // DADO
            String idMensagem = "msg-1";
            MensagemDTO mensagem = new MensagemDTO(
                    idMensagem,
                    "autor-original",
                    "dest-1",
                    "anuncio-1",
                    "Mensagem",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagem, "sucesso"));

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.excluirMensagem(idMensagem, "usuario-intruso");
                fail("Deveria ter lançado SecurityException para usuário sem permissão de excluir");
            } catch (SecurityException e) {
                assertEquals("Você não possui permissão para excluir esta mensagem", e.getMessage());
            }

            verify(chatService).buscarMensagemPorId(idMensagem);
            verify(chatService, never()).excluirMensagem(any());
            verify(chatRenderClient, never()).excluirMensagem(any());
        }

        @Test
        @DisplayName("CT18 / Resiliência - Deve lançar exceção quando ambas as instâncias falharem na exclusão")
        void deveLancarExcecao_quandoAmbasAsInstanciasFalharemNaExclusao() {
            // DADO
            String idMensagem = "msg-1";
            String uidRemetente = "user-autor";
            MensagemDTO mensagem = new MensagemDTO(
                    idMensagem,
                    uidRemetente,
                    "dest-1",
                    "anuncio-1",
                    "Mensagem",
                    LocalDateTime.now()
            );

            when(chatService.buscarMensagemPorId(idMensagem))
                    .thenReturn(new ChatResponse<>(mensagem, "sucesso"));
            doThrow(new RuntimeException("Azure Down na exclusão"))
                    .when(chatService).excluirMensagem(idMensagem);
            doThrow(new RuntimeException("Render Down na exclusão"))
                    .when(chatRenderClient).excluirMensagem(idMensagem);

            // QUANDO / ENTÃO (Tratamento defensivo)
            try {
                conversaService.excluirMensagem(idMensagem, uidRemetente);
                fail("Deveria ter propagado exceção quando ambas as nuvens falharem na exclusão");
            } catch (RuntimeException e) {
                assertEquals("Render Down na exclusão", e.getMessage());
            }

            verify(chatService).excluirMensagem(idMensagem);
            verify(chatRenderClient).excluirMensagem(idMensagem);
        }
    }
}

