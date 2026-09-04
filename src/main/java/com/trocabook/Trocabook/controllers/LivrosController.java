package com.trocabook.Trocabook.controllers;

import com.trocabook.Trocabook.model.Usuario;
import com.trocabook.Trocabook.model.UsuarioLivro;
import com.trocabook.Trocabook.repository.UsuarioLivroRepository;
import com.trocabook.Trocabook.service.ILivroService; // 1. Importar o LivroService
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Importar PostMapping
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile; // Importar MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Importar RedirectAttributes

import java.io.IOException; // Importar IOException
import java.util.LinkedList;
import java.util.Set;

@Controller
public class LivrosController {

    @Autowired
    private UsuarioLivroRepository usuarioLivroRepository;

    // 2. Injetar o LivroService que vai conter a lógica de negócio
    @Autowired
    private ILivroService livroService;

    private final Set<String> filtrosValidos = Set.of("TROCA", "VENDA", "AMBOS");

    @GetMapping("/livros")
    public String livros(Model model, HttpSession sessao, @RequestParam(value = "livroTipo", defaultValue = "todos") String filtroLivros) {
        // ... (seu método de exibir os livros continua igual) ...
        Usuario usuario = (Usuario) sessao.getAttribute("usuarioLogado");
        if (usuario != null){
            model.addAttribute("usuario", usuario);
        }
        Iterable<UsuarioLivro> usuarioLivros;
        if (filtrosValidos.contains(filtroLivros)){
            usuarioLivros = usuarioLivroRepository.findByTipoNegociacao(UsuarioLivro.TipoNegociacao.valueOf(filtroLivros));
        } else {
            usuarioLivros = usuarioLivroRepository.findAll();
        }
        LinkedList<String[]> listaUsuariosLivros = new LinkedList<>();
        for (UsuarioLivro usuarioLivro : usuarioLivros){
            String[] usuariosLivrosInfo = new String[6];
            usuariosLivrosInfo[0] = usuarioLivro.getLivro().getCapa();
            usuariosLivrosInfo[1] = usuarioLivro.getLivro().getNmLivro();
            usuariosLivrosInfo[2] = usuarioLivro.getUsuario().getFoto();
            usuariosLivrosInfo[3] = usuarioLivro.getUsuario().getNmUsuario();
            usuariosLivrosInfo[4] = Integer.toString(usuarioLivro.getCdUsuarioLivro());
            usuariosLivrosInfo[5] = Integer.toString(usuarioLivro.getUsuario().getCdUsuario());
            listaUsuariosLivros.add(usuariosLivrosInfo);

        }
        model.addAttribute("filtroLivros", filtroLivros);
        model.addAttribute("listaUsuariosLivros", listaUsuariosLivros);
        return "livros";
    }

    /*// 3. NOVO MÉTODO PARA CADASTRAR O LIVRO
    @PostMapping("/livros/cadastrar")
    public String cadastrarLivro(@RequestParam("nmLivro") String nmLivro,
                                 @RequestParam("anoPublicacao") Integer anoPublicacao,
                                 @RequestParam("capa") MultipartFile capaArquivo,
                                 RedirectAttributes attributes) {

        try {
            // O controller apenas chama o serviço, que faz todo o trabalho pesado
            livroService.cadastrarNovoLivro(nmLivro, anoPublicacao, capaArquivo);
            attributes.addFlashAttribute("sucesso", "Livro cadastrado com sucesso!");
            return "redirect:/livros"; // Redireciona para a lista de livros após o cadastro
        } catch (IOException e) {
            attributes.addFlashAttribute("erro", "Erro ao salvar a capa do livro.");
            // Idealmente, redirecionar de volta para o formulário de cadastro
            return "redirect:/pagina-de-cadastro-de-livro";
        }
    }

     */
}