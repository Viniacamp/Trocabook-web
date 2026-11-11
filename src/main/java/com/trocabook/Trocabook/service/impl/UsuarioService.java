package com.trocabook.Trocabook.service.impl;

import com.google.cloud.Timestamp;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.model.dto.*;
import com.trocabook.Trocabook.repository.UsuarioRepository;
import com.trocabook.Trocabook.service.IUsuarioService;
import com.trocabook.Trocabook.service.feign.ChatNotificacaoRenderClient;
import com.trocabook.Trocabook.service.feign.ChatNotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;

    private final FirebaseAuthService firebaseAuthService;

    private final ChatNotificacaoService chatNotificacaoService;

    private final ChatNotificacaoRenderClient chatNotificacaoRenderClient;

    public UsuarioService(UsuarioRepository usuarioRepository, FirebaseAuthService firebaseAuthService, ChatNotificacaoService chatNotificacaoService, ChatNotificacaoRenderClient chatNotificacaoRenderClient) {
        this.usuarioRepository = usuarioRepository;
        this.firebaseAuthService = firebaseAuthService;
        this.chatNotificacaoService = chatNotificacaoService;
        this.chatNotificacaoRenderClient = chatNotificacaoRenderClient;
    }

    @Override
    public void cadastrar(UsuarioInput input) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuthService.criarUsuario(
                input.emailPrincipal(),
                input.senha()
        );

        Usuario entidade = Usuario.from(input);
        entidade.setId(userRecord.getUid());
        entidade.setRole(Usuario.Role.USER);
        entidade.setStatus(Usuario.StatusUsuario.ATIVO);
        entidade.setDataCriacao(Timestamp.now());

        usuarioRepository.cadastrar(entidade);

    }

    @Override
    public UsuarioOutput buscarPorUid(String uid) {
        Usuario usuario = usuarioRepository.buscarPorUid(uid);

        if (usuario == null || usuario.getStatus() == Usuario.StatusUsuario.INATIVO){
            return null;
        }

        return usuario.paraOutput();
    }

    @Override
    public UsuarioOutput atualizar(String uid, UsuarioInput input) {
        Usuario usuarioExistente = usuarioRepository.buscarPorUid(uid);

        if (usuarioExistente == null || usuarioExistente.getStatus() == Usuario.StatusUsuario.INATIVO) {
            return null;
        }

        Usuario entidade = Usuario.from(input);

        entidade.setId(uid);
        entidade.setRole(usuarioExistente.getRole());
        entidade.setStatus(usuarioExistente.getStatus());
        entidade.setDataCriacao(
                usuarioExistente.getDataCriacao()
        );

        Usuario entidadeSalva = usuarioRepository.atualizar(entidade);

        return entidadeSalva.paraOutput();
    }


    @Override
    public void deletar(String uid) {
        if (usuarioRepository.buscarPorUid(uid) != null){
            usuarioRepository.deletar(uid);
        }
    }

    @Override
    public boolean existeComEmail(String email) {
        return usuarioRepository.buscarPorEmail(email) != null;
    }

    @Override
    public boolean existeComCpf(String cpf) {
        return usuarioRepository.buscarPorCpf(cpf) != null;
    }

    @Override
    public UsuarioOutput buscarPorEmail(String email) {
        Usuario usuario =
                usuarioRepository.buscarPorEmail(email);

        if (usuario == null) {
            return null;
        }

        return usuario.paraOutput();
    }

    @Override
    public void atualizarFcmToken(String uid, String fcmToken) {
        Usuario usuario = usuarioRepository.buscarPorUid(uid);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado para atualizar o token FCM.");
        }

        usuario.setFcmToken(fcmToken);
        usuarioRepository.atualizar(usuario);

        UsuarioStatusNotificacaoDTO dto = new UsuarioStatusNotificacaoDTO(
                usuario.getId(),
                usuario.getEmailPrincipal(),
                fcmToken
        );
        try {


            chatNotificacaoService.sincronizarStatusUsuario(dto);
            log.info("Token FCM do usuário '{}' sincronizado com o microsserviço de chat.", uid);
        } catch (Exception e) {
            log.error("Falha ao sincronizar status do usuário com o microsserviço de chat: {}", e.getMessage(), e);
            log.info("Tentando conexão com render");
            try {
                chatNotificacaoRenderClient.sincronizarStatusUsuario(dto);
                log.info("Token FCM do usuário '{}' sincronizado com o microsserviço de chat render.", uid);
            } catch (Exception ex) {
                log.error("Falha ao sincronizar status do usuário com o microsserviço de chat render: {}", ex.getMessage(), e);
            }
        }
    }

    @Override
    public List<UsuarioOutput> buscarMelhoresAvaliados() {
        return usuarioRepository.buscaTop6Avaliacao().stream()
                .map(Usuario::paraOutput)
                .toList();
    }

    @Override
    public List<UsuarioOutput> buscarTodos() {
        return usuarioRepository.buscarTodos()
                .stream()
                .map(Usuario::paraOutput)
                .toList();
    }

    @Override
    public List<UsuarioAnalyticsDTO> buscarTodasDataCriacao(Usuario.Role role) {
        return usuarioRepository.listarTodasDataCriacao(role)
                .stream()
                .map(this::transformarTimeStampParaLocalDateTime)
                .map(this::converterDateTimeParaUsuarioAnalyticsDTO)
                .toList();
    }

    @Override
    public List<AdminUsuarioOutput> buscarTodosComoAdmin() {
        return usuarioRepository.buscarTodos()
                .stream()
                .map(Usuario::paraAdminOutput)
                .toList();
    }

    @Override
    public AdminUsuarioOutput buscarPorUidComoAdmin(String uid) {
        Usuario usuario = usuarioRepository.buscarPorUid(uid);

        if (usuario == null) {
            return null;
        }

        return usuario.paraAdminOutput();
    }

    @Override
    public AdminUsuarioOutput atualizarComoAdmin(
            String uid,
            AdminUsuarioInput input
    ) throws FirebaseAuthException {

        Usuario usuario = usuarioRepository.buscarPorUid(uid);

        if (usuario == null) {
            return null;
        }

        if (usuario.getRole() == Usuario.Role.ADMIN) {
            throw new IllegalStateException(
                    "Não é permitido alterar outro administrador."
            );
        }

        Usuario.StatusUsuario statusAnterior =
                usuario.getStatus();

        if (statusAnterior != input.status()) {
            boolean desabilitado =
                    input.status() == Usuario.StatusUsuario.INATIVO;

            firebaseAuthService.alterarStatusUsuario(
                    uid,
                    desabilitado
            );
        }

        usuario.setStatus(input.status());
        usuario.setAvaliacao(input.avaliacao());

        Usuario usuarioAtualizado =
                usuarioRepository.atualizar(usuario);

        return usuarioAtualizado.paraAdminOutput();
    }

    private LocalDateTime transformarTimeStampParaLocalDateTime(Timestamp timestamp){
        return LocalDateTime.ofInstant(timestamp.toDate().toInstant(), ZoneId.of("America/Sao_Paulo"));
    }

    private UsuarioAnalyticsDTO converterDateTimeParaUsuarioAnalyticsDTO(LocalDateTime localDateTime){
        return new UsuarioAnalyticsDTO(localDateTime);
    }
}
