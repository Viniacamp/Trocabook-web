package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
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
        UsuarioOutput usuarioFirebase = usuarioService.buscarPorUid(uidUsuario);

        if (usuarioFirebase == null) {
            return null;
        }

        Livro livro = livroService.buscarPorUid(uidLivro);

        if (livro == null){
            return null;
        }

        Anuncio anuncio = new Anuncio(
                UUID.randomUUID().toString(),
                uidUsuario,
                uidLivro,
                usuarioFirebase.nome(),
                Anuncio.TipoNegociacao.valueOf(tipoNegociacao),
                livro.getTitulo(),
                usuarioFirebase.fotoPerfil(),
                livro.getUrlImagem(),
                livro.getIdsAutores(),
                livro.getIdsCategorias()

        );

        anuncioRepository.salvar(anuncio);

        return anuncio.paraDto();

    }

    @Override
    public AnuncioDTO buscarPorUid(String uid) {
        Anuncio anuncio = anuncioRepository.buscarPorUid(uid);

        if (anuncio == null){
            return null;
        }
        return anuncio.paraDto();
    }

    @Override
    public List<AnuncioDTO> listarTodos() {
        return anuncioRepository
                .listarTodos()
                .stream()
                .map(Anuncio::paraDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarTodosPorTipoNegociacao(Anuncio.TipoNegociacao tipoNegociacao) {
        return anuncioRepository
                .listarTodosPorTipoNegociacao(tipoNegociacao)
                .stream()
                .map(Anuncio::paraDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarAnunciosUsuario(String uidUsuario) {
        return anuncioRepository.buscarPorUidUsuario(uidUsuario)
                .stream()
                .map(Anuncio::paraDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> listarAnunciosUsuarioETipo(String uidUsuario, Anuncio.TipoNegociacao tipoNegociacao) {
        return anuncioRepository
                .buscarPorUidUsuarioETipoNegociacao(uidUsuario, tipoNegociacao)
                .stream()
                .map(Anuncio::paraDto)
                .toList();
    }

    @Override
    public List<AnuncioDTO> buscarPorTitulo(String titulo) {
        return anuncioRepository
                .buscarPorTitulo(titulo)
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
