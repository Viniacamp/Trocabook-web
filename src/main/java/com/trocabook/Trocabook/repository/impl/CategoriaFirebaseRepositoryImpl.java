package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.trocabook.Trocabook.model.CategoriaFirebase;
import com.trocabook.Trocabook.repository.CategoriaFirebaseRepository;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class CategoriaFirebaseRepositoryImpl implements CategoriaFirebaseRepository {
    private static final String COLECAO = "categorias";

    private final Firestore firestore;

    public CategoriaFirebaseRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public CategoriaFirebase salvar(CategoriaFirebase categoriaFirebase) {
        try {
            firestore
                    .collection(COLECAO)
                    .document(categoriaFirebase.getId())
                    .set(categoriaFirebase)
                    .get();

            return categoriaFirebase;
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao salvar categoria", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao salvar categoria", e);
        }
    }

    @Override
    public CategoriaFirebase buscarPorUid(String uid) {
        try{
            var documento = firestore
                    .collection(COLECAO)
                    .document(uid)
                    .get()
                    .get();

            if (!documento.exists()){
                return null;
            }

            return documento.toObject(CategoriaFirebase.class);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar categoria", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar categoria", e);
        }

    }

    @Override
    public CategoriaFirebase buscarPorNome(String nome) {
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
                    .toObject(CategoriaFirebase.class);

        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar categoria por nome", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro buscar categoria por nome", e);
        }
    }
}
