package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.trocabook.Trocabook.model.Autor;
import com.trocabook.Trocabook.repository.AutorRepository;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class AutorRepositoryImpl implements AutorRepository {
    private static final String COLECAO = "autores";

    private final Firestore firestore;

    public AutorRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Autor salvar(Autor autor) {
        try {
            firestore
                    .collection(COLECAO)
                    .document(autor.getId())
                    .set(autor)
                    .get();

            return autor;

        }catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao salvar autor", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao salvar autor", e);
        }
    }

    @Override
    public Autor buscarPorUid(String uid) {

        try {
            var documento = firestore
                    .collection(COLECAO)
                    .document(uid)
                    .get()
                    .get();

            if (!documento.exists()){
                return null;
            }

            return documento.toObject(Autor.class);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar autor", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar autor", e);
        }
    }

    @Override
    public Autor buscarPorNome(String nome) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("nome", nome)
                    .limit(1)
                    .get()
                    .get();

            if (documentos.isEmpty()) {
                return null;
            }

            return documentos.getDocuments()
                    .getFirst()
                    .toObject(Autor.class);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar autor por nome", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar autor por nome", e);
        }
    }
}
