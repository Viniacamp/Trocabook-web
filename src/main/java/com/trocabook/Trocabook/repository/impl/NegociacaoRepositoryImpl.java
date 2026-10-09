package com.trocabook.Trocabook.repository.impl;

import com.google.cloud.firestore.Firestore;
import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.repository.NegociacaoRepository;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public List<Negociacao> buscarPorAnuncioId(String anuncioId) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("anuncioId", anuncioId)
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
                    ("Thread interrompida ao buscar negociação por uid de anuncio", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao buscar negociação por uid de anuncio", e);
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
                    .whereEqualTo(
                            "usuarioAnuncianteId",
                            uidUsuario
                    )
                    .whereEqualTo(
                            "tipoNegociacao",
                            tipoNegociacao.name()
                    )
                    .whereEqualTo(
                            "status",
                            Negociacao.StatusNegociacao.FINALIZADA.name()
                    )
                    .get()
                    .get();

            var negociacoesComoComprador = firestore
                    .collection(COLECAO)
                    .whereEqualTo("usuarioCompradorId", uidUsuario)
                    .whereEqualTo("tipoNegociacao", tipoNegociacao.name())
                    .whereEqualTo(
                            "status",
                            Negociacao.StatusNegociacao.FINALIZADA.name()
                    )
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

    @Override
    public Negociacao buscarPorAnuncioEComprador(
            String anuncioId,
            String usuarioCompradorId
    ) {
        try {

            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("anuncioId", anuncioId)
                    .whereEqualTo(
                            "usuarioCompradorId",
                            usuarioCompradorId
                    )
                    .limit(1)
                    .get()
                    .get();

            if (documentos.isEmpty()) {
                return null;
            }

            return documentos
                    .getDocuments()
                    .getFirst()
                    .toObject(Negociacao.class);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar negociação por anuncio e comprador",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar negociação por anuncio e comprador",
                    e
            );
        }
    }

    @Override
    public List<Negociacao> buscarPorAnuncioOferecidoId(String anuncioOferecidoId) {
        try {
            var documentos = firestore
                    .collection(COLECAO)
                    .whereEqualTo("anuncioOferecidoId",
                            anuncioOferecidoId)
                    .get()
                    .get();


            return documentos
                    .getDocuments()
                    .stream()
                    .map(d -> d.toObject(Negociacao.class))
                    .toList();


        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar negociação pelo id do anuncio oferecido",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar negociação pelo id do anuncio oferecido",
                    e
            );
        }
    }

    @Override
    public List<Negociacao> buscarFinalizadasPorUsuario(
            String uidUsuario
    ) {
        try {

            var comoAnunciante = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "usuarioAnuncianteId",
                            uidUsuario
                    )
                    .whereEqualTo(
                            "status",
                            Negociacao.StatusNegociacao.FINALIZADA.name()
                    )
                    .get()
                    .get();

            var comoComprador = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "usuarioCompradorId",
                            uidUsuario
                    )
                    .whereEqualTo(
                            "status",
                            Negociacao.StatusNegociacao.FINALIZADA.name()
                    )
                    .get()
                    .get();

            Map<String, Negociacao> negociacoes =
                    new LinkedHashMap<>();

            comoAnunciante
                    .getDocuments()
                    .forEach(documento -> {

                        Negociacao negociacao =
                                documento.toObject(
                                        Negociacao.class
                                );

                        negociacoes.put(
                                negociacao.getId(),
                                negociacao
                        );
                    });

            comoComprador
                    .getDocuments()
                    .forEach(documento -> {

                        Negociacao negociacao =
                                documento.toObject(
                                        Negociacao.class
                                );

                        negociacoes.put(
                                negociacao.getId(),
                                negociacao
                        );
                    });

            return List.copyOf(
                    negociacoes.values()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar negociações finalizadas do usuário",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar negociações finalizadas do usuário",
                    e
            );
        }
    }

    @Override
    public List<Negociacao> buscarFinalizadasPorUsuarioETipo(
            String uidUsuario,
            Negociacao.TipoNegociacao tipoNegociacao
    ) {
        try {

            var comoAnunciante = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "usuarioAnuncianteId",
                            uidUsuario
                    )
                    .whereEqualTo(
                            "tipoNegociacao",
                            tipoNegociacao.name()
                    )
                    .whereEqualTo(
                            "status",
                            Negociacao.StatusNegociacao.FINALIZADA.name()
                    )
                    .get()
                    .get();

            var comoComprador = firestore
                    .collection(COLECAO)
                    .whereEqualTo(
                            "usuarioCompradorId",
                            uidUsuario
                    )
                    .whereEqualTo(
                            "tipoNegociacao",
                            tipoNegociacao.name()
                    )
                    .whereEqualTo(
                            "status",
                            Negociacao.StatusNegociacao.FINALIZADA.name()
                    )
                    .get()
                    .get();

            Map<String, Negociacao> negociacoes =
                    new LinkedHashMap<>();

            comoAnunciante
                    .getDocuments()
                    .forEach(documento -> {

                        Negociacao negociacao =
                                documento.toObject(
                                        Negociacao.class
                                );

                        negociacoes.put(
                                negociacao.getId(),
                                negociacao
                        );
                    });

            comoComprador
                    .getDocuments()
                    .forEach(documento -> {

                        Negociacao negociacao =
                                documento.toObject(
                                        Negociacao.class
                                );

                        negociacoes.put(
                                negociacao.getId(),
                                negociacao
                        );
                    });

            return List.copyOf(
                    negociacoes.values()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrompida ao buscar negociações finalizadas por tipo",
                    e
            );

        } catch (ExecutionException e) {

            throw new RuntimeException(
                    "Erro ao buscar negociações finalizadas por tipo",
                    e
            );
        }
    }
}
