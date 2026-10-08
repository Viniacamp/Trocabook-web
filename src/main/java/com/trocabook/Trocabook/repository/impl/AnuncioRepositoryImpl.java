package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.repository.AnuncioRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class AnuncioRepositoryImpl implements AnuncioRepository {
    private static final String COLECAO = "anuncios";

    private final Firestore firestore;

    public AnuncioRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Anuncio salvar(Anuncio anuncio) {
        try {
            firestore
                    .collection(COLECAO)
                    .document(anuncio.getId())
                    .set(anuncio)
                    .get();

            return anuncio;
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao salvar anuncio", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao salvar anuncio", e);
        }

    }

    @Override
    public List<Anuncio> listarTodos() {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .get()
                    .get()
                    .getDocuments();

            return documentos.stream()
                    .map(d -> d.toObject(Anuncio.class))
                    .toList();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar todos os anuncios", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar todos os anuncios", e);
        }
    }

    @Override
    public List<Anuncio> listarTodosPorTipoNegociacao(
            Anuncio.TipoNegociacao tipoNegociacao,
            Anuncio.StatusAnuncio status
    ) {
        try {
            Query query = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "tipoNegociacao",
                            tipoNegociacao.name()
                    );

            if (status != null) {
                query = query.whereEqualTo(
                        "status",
                        status.name()
                );
            }

            var documentos = query
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Anuncio.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar anúncio por tipo de negociação",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar anúncio por tipo de negociação",
                    e
            );
        }
    }

    @Override
    public Anuncio buscarPorUid(String uid) {
        try {
            var documento =
                    firestore
                            .collection(COLECAO)
                            .document(uid)
                            .get()
                            .get();

            if (!documento.exists()){
                return null;
            }

            return documento.toObject(Anuncio.class);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar anuncio", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar anuncio", e);
        }
    }

    @Override
    public List<Anuncio> buscarPorUidUsuario(
            String uidUsuario,
            Anuncio.StatusAnuncio status
    ) {
        try {
            Query query = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "uidUsuario",
                            uidUsuario
                    );

            if (status != null) {
                query = query.whereEqualTo(
                        "status",
                        status.name()
                );
            }

            var documentos = query
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Anuncio.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar anúncio por uid de usuário",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar anúncio por uid de usuário",
                    e
            );
        }
    }

    @Override
    public List<Anuncio> buscarPorUidUsuarioETipoNegociacao(
            String uidUsuario,
            Anuncio.TipoNegociacao tipoNegociacao,
            Anuncio.StatusAnuncio status
    ) {
        try {
            Query query = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "uidUsuario",
                            uidUsuario
                    )
                    .whereEqualTo(
                            "tipoNegociacao",
                            tipoNegociacao.name()
                    );

            if (status != null) {
                query = query.whereEqualTo(
                        "status",
                        status.name()
                );
            }

            var documentos = query
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Anuncio.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar anúncio por usuário e tipo de negociação",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar anúncio por usuário e tipo de negociação",
                    e
            );
        }
    }

    @Override
    public List<Anuncio> buscarPorUidLivro(
            String uidLivro,
            Anuncio.StatusAnuncio status
    ) {
        try {
            Query query = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "uidLivro",
                            uidLivro
                    );

            if (status != null) {
                query = query.whereEqualTo(
                        "status",
                        status.name()
                );
            }

            var documentos = query
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Anuncio.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar anúncio por uid de livro",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar anúncio por uid de livro",
                    e
            );
        }
    }

    @Override
    public List<Anuncio> buscarPorTitulo(
            String titulo,
            Anuncio.StatusAnuncio status
    ) {
        try {
            Query query = firestore
                    .collection(COLECAO);

            if (status != null) {
                query = query.whereEqualTo(
                        "status",
                        status.name()
                );
            }

            query = query
                    .orderBy("tituloBusca")
                    .startAt(titulo)
                    .endAt(titulo + "\uf8ff");

            var documentos = query
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Anuncio.class))
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar anúncio por título",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao buscar anúncio por título",
                    e
            );
        }
    }

    @Override
    public Anuncio atualizar(Anuncio anuncio) {
        try {
            if (this.buscarPorUid(anuncio.getId()) == null){
                return null;
            }

            firestore
                    .collection(COLECAO)
                    .document(anuncio.getId())
                    .set(anuncio)
                    .get();

            return anuncio;
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao atualizar anuncio", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao atualizar anuncio", e);
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
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao deletar anuncio", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao deletar anuncio", e);
        }

    }

    @Override
    public List<Anuncio> listarAtivos() {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "status",
                            Anuncio.StatusAnuncio.ATIVO.name()
                    )
                    .get()
                    .get();

            return documentos
                    .getDocuments()
                    .stream()
                    .map(documento ->
                            documento.toObject(Anuncio.class)
                    )
                    .toList();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao listar anúncios ativos",
                    e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Erro ao listar anúncios ativos",
                    e
            );
        }
    }
}
