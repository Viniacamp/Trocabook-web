package com.trocabook.Trocabook.repository;

import com.trocabook.Trocabook.model.Usuario;

import java.util.List;

public interface UsuarioRepository {

    Usuario cadastrar(Usuario entidade);

    Usuario buscarPorUid(String uid);

    List<Usuario> buscarTodos();

    List<Usuario> buscarPorUids(List<String> uids);

    Usuario atualizar(Usuario entidade);

    void deletar(String uid);

    Usuario buscarPorEmail(String email);

    Usuario buscarPorCpf(String cpf);

    List<Usuario> buscaTop6Avaliacao();


}