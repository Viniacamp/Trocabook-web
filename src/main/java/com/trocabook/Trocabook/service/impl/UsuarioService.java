package com.trocabook.Trocabook.service.impl;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.model.dto.UsuarioInput;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.repository.UsuarioRepository;
import com.trocabook.Trocabook.service.IUsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService implements IUsuarioService {
    private final UsuarioRepository usuarioRepository;

    private final FirebaseAuthService firebaseAuthService;

    public UsuarioService(UsuarioRepository usuarioRepository, FirebaseAuthService firebaseAuthService) {
        this.usuarioRepository = usuarioRepository;
        this.firebaseAuthService = firebaseAuthService;
    }

    @Override
    public void cadastrar(UsuarioInput input) throws FirebaseAuthException {
        UserRecord userRecord = firebaseAuthService.criarUsuario(
                input.emailPrincipal(),
                input.senha()
        );

        Usuario entidade = Usuario.from(input);
        entidade.setId(userRecord.getUid());

        usuarioRepository.cadastrar(entidade);

    }

    @Override
    public UsuarioOutput buscarPorUid(String uid) {
        Usuario usuario = usuarioRepository.buscarPorUid(uid);

        if (usuario == null){
            return null;
        }

        return usuario.paraOutput();
    }

    @Override
    public UsuarioOutput atualizar(String uid, UsuarioInput input) {
        if (usuarioRepository.buscarPorUid(uid) == null){
            return null;
        }

        Usuario entidade = Usuario.from(input);

        entidade.setId(uid);

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
}
