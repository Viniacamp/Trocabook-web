package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.trocabook.Trocabook.model.LivroFirebase;
import com.trocabook.Trocabook.repository.LivroFirebaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class LivroFirebaseRepositoryImpl implements LivroFirebaseRepository {

    private static final String COLECAO = "livros";

    private final Firestore firestore;

    public LivroFirebaseRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public LivroFirebase salvar(LivroFirebase entidade) {

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
                    "Thread interrompida ao salvar livro.",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao salvar livro no Firebase.",
                    e
            );
        }
    }

    @Override
    public LivroFirebase buscarPorUid(String uid) {

        try {

            var documento =
                    firestore
                            .collection(COLECAO)
                            .document(uid)
                            .get()
                            .get();

            if (!documento.exists()) {
                return null;
            }

            return documento.toObject(LivroFirebase.class);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar livro.",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar livro no Firebase.",
                    e
            );
        }
    }

    @Override
    public List<LivroFirebase> buscarTodos() {

        try {

            QuerySnapshot resultado =
                    firestore
                            .collection(COLECAO)
                            .get()
                            .get();

            return resultado
                    .getDocuments()
                    .stream()
                    .map(documento ->
                            documento.toObject(LivroFirebase.class)
                    )
                    .toList();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar livros.",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar livros no Firebase.",
                    e
            );
        }
    }

    @Override
    public List<LivroFirebase> buscarPorTitulo(String titulo) {

        try {

            QuerySnapshot resultado =
                    firestore
                            .collection(COLECAO)
                            .whereEqualTo("titulo", titulo)
                            .get()
                            .get();

            return resultado
                    .getDocuments()
                    .stream()
                    .map(documento ->
                            documento.toObject(LivroFirebase.class)
                    )
                    .toList();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar livros por título.",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar livros por título no Firebase.",
                    e
            );
        }
    }

    @Override
    public LivroFirebase buscarPorGoogleBooksId(String googleBooksId) {

        try {

            QuerySnapshot resultado =
                    firestore
                            .collection(COLECAO)
                            .whereEqualTo("googleId", googleBooksId)
                            .limit(1)
                            .get()
                            .get();

            if (resultado.isEmpty()) {
                return null;
            }

            return resultado
                    .getDocuments()
                    .getFirst()
                    .toObject(LivroFirebase.class);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar livro pelo Google Books ID.",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar livro pelo Google Books ID no Firebase.",
                    e
            );
        }
    }
}