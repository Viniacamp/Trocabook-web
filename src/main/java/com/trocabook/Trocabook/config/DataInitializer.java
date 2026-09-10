package com.trocabook.Trocabook.config;

import com.google.firebase.auth.FirebaseAuthException;
import com.trocabook.Trocabook.model.Anuncio;
import com.trocabook.Trocabook.model.Livro;
import com.trocabook.Trocabook.model.Negociacao;
import com.trocabook.Trocabook.model.dto.AnuncioDTO;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;
import com.trocabook.Trocabook.model.dto.NegociacaoDTO;
import com.trocabook.Trocabook.model.dto.UsuarioInput;
import com.trocabook.Trocabook.model.dto.UsuarioOutput;
import com.trocabook.Trocabook.service.IAnuncioService;
import com.trocabook.Trocabook.service.ILivroService;
import com.trocabook.Trocabook.service.INegociacaoService;
import com.trocabook.Trocabook.service.IUsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final IUsuarioService usuarioService;
    private final ILivroService livroService;
    private final IAnuncioService anuncioService;
    private final INegociacaoService negociacaoService;

    public DataInitializer(
            IUsuarioService usuarioService,
            ILivroService livroService,
            IAnuncioService anuncioService,
            INegociacaoService negociacaoService
    ) {
        this.usuarioService = usuarioService;
        this.livroService = livroService;
        this.anuncioService = anuncioService;
        this.negociacaoService = negociacaoService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioService.buscarTodos().isEmpty()) {
            System.out.println("Dados iniciais já existentes. Inicialização ignorada.");
            return;
        }

        // ============================================================
        // USUÁRIOS
        // ============================================================

        UsuarioOutput squirtle = criarUsuario(
                new UsuarioInput(
                        "squirtle",
                        "087.382.730-98",
                        "squirtle@gmail.com",
                        null,
                        "Squirtle@123",
                        "/img/vendedor1.svg",
                        null,
                        LocalDate.of(2000, 1, 1),
                        "123456789",
                        "(11) 99999-1111"
                )
        );

        UsuarioOutput pedroLucas = criarUsuario(
                new UsuarioInput(
                        "PedroLucas",
                        "796.895.940-36",
                        "pedrol@gmail.com",
                        null,
                        "Pedro@456",
                        "/img/vendedor2.svg",
                        null,
                        LocalDate.of(2000, 2, 2),
                        "234567890",
                        "(11) 99999-2222"
                )
        );

        UsuarioOutput rafaela = criarUsuario(
                new UsuarioInput(
                        "Rafaela",
                        "336.443.280-56",
                        "rafa@gmail.com",
                        null,
                        "Rafaela@789",
                        "/img/vendedor3.svg",
                        null,
                        LocalDate.of(2000, 3, 3),
                        "345678901",
                        "(11) 99999-3333"
                )
        );

        UsuarioOutput vinicius = criarUsuario(
                new UsuarioInput(
                        "Vinicius",
                        "189.621.590-40",
                        "vini@gmail.com",
                        null,
                        "Vini@101",
                        "/img/vendedor4.svg",
                        null,
                        LocalDate.of(2000, 4, 4),
                        "456789012",
                        "(11) 99999-4444"
                )
        );

        UsuarioOutput wellington = criarUsuario(
                new UsuarioInput(
                        "Welligton",
                        "801.049.840-82",
                        "well@gmail.com",
                        null,
                        "Well@202",
                        "/img/vendedor5.svg",
                        null,
                        LocalDate.of(2000, 5, 5),
                        "567890123",
                        "(11) 99999-5555"
                )
        );

        UsuarioOutput gpt = criarUsuario(
                new UsuarioInput(
                        "Gpt",
                        "598.833.780-50",
                        "gpt@gmail.com",
                        null,
                        "GptBot@2025!",
                        "/img/vendedor6.svg",
                        null,
                        LocalDate.of(2000, 6, 6),
                        "678901234",
                        "(11) 99999-6666"
                )
        );

        // ============================================================
        // LIVROS
        // ============================================================

        Livro livro1 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-1",
                        "A Era da IA: e nosso futuro como humanos",
                        List.of("Henry Kissinger"),
                        "Companhia das Letras",
                        "1980",
                        "/img/IALIVRO.png",
                        "pt",
                        List.of("Tecnologia", "Inteligência Artificial")
                )
        );

        Livro livro2 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-2",
                        "A Floresta Sombria",
                        List.of("Liu Cixin"),
                        "Suma",
                        "1980",
                        "/img/livro2.png",
                        "pt",
                        List.of("Ficção Científica")
                )
        );

        Livro livro3 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-3",
                        "A Culpa é das Estrelas",
                        List.of("John Green"),
                        "Intrínseca",
                        "1980",
                        "/img/culpa.jpg",
                        "pt",
                        List.of("Romance")
                )
        );

        Livro livro4 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-4",
                        "O Pequeno Príncipe",
                        List.of("Antoine de Saint-Exupéry"),
                        "Agir",
                        "1980",
                        "/img/livro4.png",
                        "pt",
                        List.of("Literatura", "Infantil")
                )
        );

        Livro livro5 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-5",
                        "365 Reflexões Estóicas",
                        List.of("Ryan Holiday"),
                        "Alta Books",
                        "1980",
                        "/img/livro6.png",
                        "pt",
                        List.of("Filosofia")
                )
        );

        Livro livro6 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-6",
                        "Dom Casmurro",
                        List.of("Machado de Assis"),
                        "Penguin-Companhia",
                        "1980",
                        "/img/dom casmurro.jpg",
                        "pt",
                        List.of("Literatura Brasileira")
                )
        );

        Livro livro7 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-7",
                        "Coletânea Harry Potter",
                        List.of("J. K. Rowling"),
                        "Rocco",
                        "1980",
                        "/img/harry.jpg",
                        "pt",
                        List.of("Fantasia")
                )
        );

        Livro livro8 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-8",
                        "Fundamentos de HTML5 e CSS3",
                        List.of("Eric Freeman", "Elisabeth Robson"),
                        "Alta Books",
                        "1980",
                        "/img/capa-ampliada-9788575224380.jpg",
                        "pt",
                        List.of("Programação", "Web")
                )
        );

        Livro livro9 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-9",
                        "Sommerville - Engenharia de Software",
                        List.of("Ian Sommerville"),
                        "Pearson",
                        "1980",
                        "/img/engenharia de software.jpg",
                        "pt",
                        List.of("Engenharia de Software")
                )
        );

        Livro livro10 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-10",
                        "Livro Senhor dos Aneis A Sociedade do Anel",
                        List.of("J. R. R. Tolkien"),
                        "HarperCollins",
                        "1980",
                        "/img/61460909.jpg",
                        "pt",
                        List.of("Fantasia")
                )
        );

        Livro livro11 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-11",
                        "Dracula",
                        List.of("Bram Stoker"),
                        "Penguin",
                        "1980",
                        "/img/livro5.png",
                        "pt",
                        List.of("Terror", "Literatura")
                )
        );

        Livro livro12 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-12",
                        "Harry Potter e a Pedra Filosofal",
                        List.of("J. K. Rowling"),
                        "Rocco",
                        "1980",
                        "/img/livro3.png",
                        "pt",
                        List.of("Fantasia")
                )
        );

        Livro livro13 = cadastrarLivro(
                new LivroBuscaOutput(
                        "seed-livro-13",
                        "A Garota do Lago",
                        List.of("Charlie Donlea"),
                        "Faro Editorial",
                        "1980",
                        "/img/image-15@2x.png",
                        "pt",
                        List.of("Suspense", "Mistério")
                )
        );

        // ============================================================
        // ANÚNCIOS
        // ============================================================

        AnuncioDTO anuncio1 = criarAnuncio(
                livro1,
                squirtle,
                Anuncio.TipoNegociacao.TROCA
        );

        AnuncioDTO anuncio2 = criarAnuncio(
                livro2,
                squirtle,
                Anuncio.TipoNegociacao.VENDA
        );

        AnuncioDTO anuncio3 = criarAnuncio(
                livro3,
                pedroLucas,
                Anuncio.TipoNegociacao.TROCA
        );

        AnuncioDTO anuncio4 = criarAnuncio(
                livro4,
                pedroLucas,
                Anuncio.TipoNegociacao.TROCA
        );

        AnuncioDTO anuncio5 = criarAnuncio(
                livro5,
                rafaela,
                Anuncio.TipoNegociacao.AMBOS
        );

        AnuncioDTO anuncio6 = criarAnuncio(
                livro6,
                rafaela,
                Anuncio.TipoNegociacao.TROCA
        );

        AnuncioDTO anuncio7 = criarAnuncio(
                livro7,
                vinicius,
                Anuncio.TipoNegociacao.VENDA
        );

        AnuncioDTO anuncio8 = criarAnuncio(
                livro8,
                vinicius,
                Anuncio.TipoNegociacao.AMBOS
        );

        AnuncioDTO anuncio9 = criarAnuncio(
                livro9,
                wellington,
                Anuncio.TipoNegociacao.TROCA
        );

        AnuncioDTO anuncio10 = criarAnuncio(
                livro10,
                wellington,
                Anuncio.TipoNegociacao.VENDA
        );

        AnuncioDTO anuncio11 = criarAnuncio(
                livro11,
                squirtle,
                Anuncio.TipoNegociacao.TROCA
        );

        AnuncioDTO anuncio12 = criarAnuncio(
                livro12,
                rafaela,
                Anuncio.TipoNegociacao.VENDA
        );

        AnuncioDTO anuncio13 = criarAnuncio(
                livro13,
                rafaela,
                Anuncio.TipoNegociacao.TROCA
        );

        // ============================================================
        // NEGOCIAÇÕES
        // ============================================================

        criarNegociacao(
                anuncio11,
                squirtle,
                pedroLucas,
                Negociacao.TipoNegociacao.TROCA
        );

        criarNegociacao(
                anuncio12,
                rafaela,
                vinicius,
                Negociacao.TipoNegociacao.VENDA
        );

        criarNegociacao(
                anuncio13,
                rafaela,
                wellington,
                Negociacao.TipoNegociacao.TROCA
        );

        System.out.println("==========================================");
        System.out.println("Dados iniciais do Trocabook carregados.");
        System.out.println("==========================================");
    }

    // ================================================================
    // USUÁRIO
    // ================================================================

    private UsuarioOutput criarUsuario(
            UsuarioInput input
    ) throws FirebaseAuthException {

        if (!usuarioService.existeComEmail(input.emailPrincipal())) {
            usuarioService.cadastrar(input);
        }

        return usuarioService.buscarPorEmail(input.emailPrincipal());
    }

    // ================================================================
    // LIVRO
    // ================================================================

    private Livro cadastrarLivro(
            LivroBuscaOutput livroInput
    ) {

        Livro livroExistente =
                livroService.buscarPorGoogleBooksId(
                        livroInput.googleBooksId()
                );

        if (livroExistente != null) {
            return livroExistente;
        }

        return livroService.cadastrar(livroInput);
    }

    // ================================================================
    // ANÚNCIO
    // ================================================================

    private AnuncioDTO criarAnuncio(
            Livro livro,
            UsuarioOutput usuario,
            Anuncio.TipoNegociacao tipoNegociacao
    ) {

        List<AnuncioDTO> anunciosUsuario =
                anuncioService.listarAnunciosUsuario(usuario.id());

        for (AnuncioDTO anuncio : anunciosUsuario) {

            if (anuncio.uidLivro().equals(livro.getId())
                    && anuncio.tipoNegociacao().equals(tipoNegociacao.name())) {

                return anuncio;
            }
        }

        return anuncioService.anunciar(
                livro.getId(),
                usuario.id(),
                tipoNegociacao.name()
        );
    }

    // ================================================================
    // NEGOCIAÇÃO
    // ================================================================

    private void criarNegociacao(
            AnuncioDTO anuncio,
            UsuarioOutput anunciante,
            UsuarioOutput comprador,
            Negociacao.TipoNegociacao tipoNegociacao
    ) {

        List<NegociacaoDTO> negociacoes =
                negociacaoService.listarPorUsuarioAnunciante(
                        anunciante.id()
                );

        for (NegociacaoDTO negociacao : negociacoes) {

            if (negociacao.anuncioId().equals(anuncio.id())
                    && negociacao.usuarioCompradorId().equals(comprador.id())) {

                return;
            }
        }

        NegociacaoDTO negociacaoDTO = new NegociacaoDTO(
                null,
                anunciante.id(),
                comprador.id(),
                anuncio.id(),
                LocalDateTime.now(),
                tipoNegociacao.name(),
                anunciante.nome(),
                anunciante.fotoPerfil(),
                comprador.nome(),
                comprador.fotoPerfil(),
                anuncio.titulo(),
                anuncio.capa()
        );

        negociacaoService.salvar(negociacaoDTO);
    }
}