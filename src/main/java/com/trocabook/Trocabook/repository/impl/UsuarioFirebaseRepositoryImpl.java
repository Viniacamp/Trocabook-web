package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.trocabook.Trocabook.model.UsuarioFirebase;
import com.trocabook.Trocabook.repository.UsuarioFirebaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Repository
public class UsuarioFirebaseRepositoryImpl
        implements UsuarioFirebaseRepository {

    private final Firestore firestore;

    private static final String COLECAO = "usuarios";

    public UsuarioFirebaseRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public UsuarioFirebase cadastrar(UsuarioFirebase entidade) {
        try {
            firestore
                    .collection(COLECAO)
                    .document(entidade.getId())
                    .set(entidade)
                    .get();

            return entidade;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Operação interrompida ao cadastrar usuário.", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao cadastrar usuário no Firebase.", e
            );
        }
    }

    @Override
    public UsuarioFirebase buscarPorUid(String uid) {
        try {
            DocumentSnapshot documento = firestore
                    .collection(COLECAO)
                    .document(uid)
                    .get()
                    .get();

            if (!documento.exists()) {
                return null;
            }

            return documento.toObject(UsuarioFirebase.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Operação interrompida ao buscar usuário.", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar usuário no Firebase.", e
            );
        }
    }

    @Override
    public List<UsuarioFirebase> buscarTodos() {
        try {
            List<QueryDocumentSnapshot> documentos = firestore
                    .collection(COLECAO)
                    .get()
                    .get()
                    .getDocuments();

            return documentos.stream()
                    .map(d -> d.toObject(UsuarioFirebase.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Operação interrompida ao buscar usuários.", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar usuários no Firebase.", e
            );
        }
    }

    @Override
    public List<UsuarioFirebase> buscarPorUids(List<String> uids) {
        return uids.stream()
                .map(this::buscarPorUid)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public UsuarioFirebase atualizar(UsuarioFirebase entidade) {
        try {
            if (this.buscarPorUid(entidade.getId()) == null) {
                return null;
            }

            firestore
                    .collection(COLECAO)
                    .document(entidade.getId())
                    .set(entidade)
                    .get();

            return entidade;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Operação interrompida ao atualizar usuário.", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao atualizar usuário no Firebase.", e
            );
        }
    }

    @Override
    public void deletar(String uid) {
        try {
            firestore
                    .collection(COLECAO)
                    .document(uid)
                    .delete()
                    .get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Operação interrompida ao deletar usuário.", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao deletar usuário no Firebase.", e
            );
        }
    }

    @Override
    public UsuarioFirebase buscarPorEmail(String email) {
        try {
            List<QueryDocumentSnapshot> documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("emailPrincipal", email)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments();

            if (documentos.isEmpty()) {
                return null;
            }

            return documentos.getFirst()
                    .toObject(UsuarioFirebase.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Operação interrompida ao buscar usuário por e-mail.", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar usuário por e-mail no Firebase.", e
            );
        }
    }

    @Override
    public UsuarioFirebase buscarPorCpf(String cpf) {
        try {
            List<QueryDocumentSnapshot> documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("cpf", cpf)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments();

            if (documentos.isEmpty()) {
                return null;
            }

            return documentos.getFirst()
                    .toObject(UsuarioFirebase.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Operação interrompida ao buscar usuário por CPF.", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar usuário por CPF no Firebase.", e
            );
        }
    }
}