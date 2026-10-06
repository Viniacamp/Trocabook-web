package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.trocabook.Trocabook.model.Categoria;
import com.trocabook.Trocabook.repository.CategoriaRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class CategoriaRepositoryImpl implements CategoriaRepository {
    private static final String COLECAO = "categorias";

    private final Firestore firestore;

    public CategoriaRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Categoria salvar(Categoria categoria) {
        try {
            firestore
                    .collection(COLECAO)
                    .document(categoria.getId())
                    .set(categoria)
                    .get();

            return categoria;
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao salvar categoria", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao salvar categoria", e);
        }
    }

    @Override
    public Categoria buscarPorUid(String uid) {
        try{
            var documento = firestore
                    .collection(COLECAO)
                    .document(uid)
                    .get()
                    .get();

            if (!documento.exists()){
                return null;
            }

            return documento.toObject(Categoria.class);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar categoria", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar categoria", e);
        }

    }

    @Override
    public Categoria buscarPorNome(String nome) {
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
                    .toObject(Categoria.class);

        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar categoria por nome", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro buscar categoria por nome", e);
        }
    }

    @Override
    public List<Categoria> buscarTodos() {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .get()
                    .get();

            return documentos.getDocuments()
                    .stream()
                    .map(documento -> documento.toObject(Categoria.class))
                    .sorted(Comparator.comparing(
                            Categoria::getNome,
                            String.CASE_INSENSITIVE_ORDER
                    ))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao listar categorias", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao listar categorias", e
            );
        }
    }
}
