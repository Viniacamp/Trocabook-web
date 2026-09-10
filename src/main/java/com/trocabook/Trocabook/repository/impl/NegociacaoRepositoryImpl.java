package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.repository.NegociacaoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class NegociacaoRepositoryImpl implements NegociacaoRepository {
    private static final String COLECAO = "negociacoes";

    private final Firestore firestore;

    public NegociacaoRepositoryImpl(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Negociacao salvar(Negociacao negociacao) {
        try{
            firestore
                    .collection(COLECAO)
                    .document(negociacao.getId())
                    .set(negociacao)
                    .get();

            return negociacao;
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao salvar negociação", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao salvar negociação", e);
        }
    }

    @Override
    public Negociacao buscarPorUid(String uid) {
        try {
            var documento = firestore
                    .collection(COLECAO)
                    .document(uid)
                    .get()
                    .get();

            if (!documento.exists()){
                return null;
            }

            return documento.toObject(Negociacao.class);


        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar negociação", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar negociação", e);
        }
    }

    @Override
    public List<Negociacao> buscarPorUidUsuarioAnunciante(String uidAnunciante) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("usuarioAnuncianteId", uidAnunciante)
                    .get()
                    .get();


            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Negociacao.class))
                    .toList();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar negociação por uid de usuário anunciante", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar negociação por uid de usuário anunciante", e);
        }
    }

    @Override
    public List<Negociacao> buscarPorUidUsuarioComprador(String uidComprador) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("usuarioCompradorId", uidComprador)
                    .get()
                    .get();


            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Negociacao.class))
                    .toList();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar negociação por uid de usuário comprador", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar negociação por uid de usuário comprador", e);
        }
    }

    @Override
    public List<Negociacao> buscarPorUidAnuncianteETipoNegociacao(String uidAnunciante, Negociacao.TipoNegociacao tipoNegociacao) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("usuarioAnuncianteId", uidAnunciante)
                    .whereEqualTo("tipoNegociacao", tipoNegociacao.name())
                    .get()
                    .get();


            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Negociacao.class))
                    .toList();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar negociação por uid de usuário anunciante e tipo de negociação", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar negociação por uid de usuário anunciante e tipo de negociação", e);
        }
    }

    @Override
    public List<Negociacao> buscarPorUidCompradorETipoNegociacao(String uidComprador, Negociacao.TipoNegociacao tipoNegociacao) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("usuarioCompradorId", uidComprador)
                    .whereEqualTo("tipoNegociacao", tipoNegociacao.name())
                    .get()
                    .get();


            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Negociacao.class))
                    .toList();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();

            throw new RuntimeException
                    ("Thread interrompida ao buscar negociação por uid de usuário comprador e tipo de negociação", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar negociação por uid de usuário comprador e tipo de negociação", e);
        }
    }

    @Override
    public long contarNegociacoesPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao
    ) {
        try {
            var negociacoesComoAnunciante = firestore
                    .collection(COLECAO)
                    .whereEqualTo("usuarioAnuncianteId", uidUsuario)
                    .whereEqualTo("tipoNegociacao", tipoNegociacao.name())
                    .get()
                    .get();

            var negociacoesComoComprador = firestore
                    .collection(COLECAO)
                    .whereEqualTo("usuarioCompradorId", uidUsuario)
                    .whereEqualTo("tipoNegociacao", tipoNegociacao.name())
                    .get()
                    .get();

            return negociacoesComoAnunciante.size()
                    + negociacoesComoComprador.size();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao contar negociações",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao contar negociações",
                    e
            );
        }
    }
}
