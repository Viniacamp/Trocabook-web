package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.trocabook.Trocabook.model.Interacao;
import com.trocabook.Trocabook.repository.InteracaoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class InteracaoRepositoryImpl implements InteracaoRepository {

    private static final String COLECAO = "interacoes";

    private final Firestore firestore;

    public InteracaoRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Interacao salvar(Interacao interacao) {
        try {
            firestore
                    .collection(COLECAO)
                    .document(interacao.getId())
                    .set(interacao)
                    .get();

            return interacao;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao salvar interação", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao salvar interação", e
            );
        }
    }

    @Override
    public List<Interacao> listarTodos() {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .get()
                    .get()
                    .getDocuments();

            return documentos
                    .stream()
                    .map(documento -> documento.toObject(Interacao.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar todas as interações", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar todas as interações", e
            );
        }
    }

    @Override
    public List<Interacao> buscarPorUidUsuario(String uidUsuario) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("uidUsuario", uidUsuario)
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(documento -> documento.toObject(Interacao.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar interações do usuário", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar interações do usuário", e
            );
        }
    }

    @Override
    public List<Interacao> buscarPorUidUsuarioETipo(
            String uidUsuario,
            Interacao.TipoInteracao tipoInteracao
    ) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("uidUsuario", uidUsuario)
                    .whereEqualTo("tipoInteracao", tipoInteracao.name())
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(documento -> documento.toObject(Interacao.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar interações do usuário por tipo",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar interações do usuário por tipo",
                    e
            );
        }
    }

    @Override
    public List<Interacao> buscarPorUidUsuarioEAnuncioETipo(
            String uidUsuario,
            String uidAnuncio,
            Interacao.TipoInteracao tipoInteracao
    ) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("uidUsuario", uidUsuario)
                    .whereEqualTo("uidAnuncio", uidAnuncio)
                    .whereEqualTo("tipoInteracao", tipoInteracao.name())
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(documento -> documento.toObject(Interacao.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao verificar interação do usuário",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao verificar interação do usuário",
                    e
            );
        }
    }
}