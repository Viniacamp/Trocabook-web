package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.LivroFirebase;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioFirebaseOutput;
import com.trocabook.Trocabook.repository.AnuncioRepository;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.IUsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnuncioService implements IAnuncioService {
    private final IUsuarioService usuarioService;

    private final ILivroService livroService;

    private final AnuncioRepository anuncioRepository;

    public AnuncioService(IUsuarioService usuarioService, ILivroService livroService, AnuncioRepository anuncioRepository) {
        this.usuarioService = usuarioService;
        this.livroService = livroService;
        this.anuncioRepository = anuncioRepository;
    }

    @Override
    public AnuncioDTO anunciar(String uidLivro, String uidUsuario, String tipoNegociacao) {
        UsuarioFirebaseOutput usuarioFirebase = usuarioService.logar(uidUsuario);

        if (usuarioFirebase == null) {
            return null;
        }

        LivroFirebase livroFirebase = livroService.buscarPorUid(uidLivro);

        if (livroFirebase == null){
            return null;
        }

        Anuncio anuncio = new Anuncio(
                UUID.randomUUID().toString(),
                uidUsuario,
                uidLivro,
                usuarioFirebase.nome(),
                Anuncio.TipoNegociacao.valueOf(tipoNegociacao),
                livroFirebase.getTitulo(),
                usuarioFirebase.fotoPerfil(),
                livroFirebase.getUrlImagem(),
                livroFirebase.getIdsAutores(),
                livroFirebase.getIdsCategorias()

        );

        anuncioRepository.salvar(anuncio);

        return anuncio.paraDto();

    }

    @Override
    public List<AnuncioDTO> listarAnunciosUsuario(String uidUsuario) {
        return anuncioRepository.buscarPorUidUsuario(uidUsuario)
                .stream()
                .map(Anuncio::paraDto)
                .toList();
    }

    @Override
    public AnuncioDTO atualizar(AnuncioDTO anuncioDTO) {
        anuncioRepository.atualizar(Anuncio.from(anuncioDTO));
        return anuncioDTO;
    }

    @Override
    public void deletar(String uid) {
        anuncioRepository.deletar(uid);

    }
}
