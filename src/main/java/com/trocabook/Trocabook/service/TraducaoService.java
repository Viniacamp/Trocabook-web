/*package com.trocabook.Trocabook.service;

import com.trocabook.Trocabook.model.Categoria;
import com.trocabook.Trocabook.model.dto.MyMemoryResponse;
import com.trocabook.Trocabook.model.dto.TraducaoRequest;
import com.trocabook.Trocabook.model.dto.TraducaoResponse;
import com.trocabook.Trocabook.repository.CategoriaRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TraducaoService {
    private static final String MYMEMORY_URL = "https://api.mymemory.translated.net/get?q=%s&langpair=%s|%s";


    private final RestTemplate restTemplate;

    private final CategoriaRepository categoriaRepository;

    private final Map<String, String> cacheCategorias = new ConcurrentHashMap<>();

    private final Map<String, String> CATEGORIAS_FIXAS =
            Map.ofEntries(
                    Map.entry("science", "Ciência"),
                    Map.entry("fantasy", "Fantasia"),
                    Map.entry("computers", "Computação"),
                    Map.entry("romance", "Romance"),
                    Map.entry("fiction", "Ficção"),
                    Map.entry("self help", "Autoajuda"),
                    Map.entry("business", "Negócios"),
                    Map.entry("history", "História")
            );



    @Autowired
    public TraducaoService(RestTemplate restTemplate, CategoriaRepository categoriaRepository) {
        this.restTemplate = restTemplate;
        this.categoriaRepository = categoriaRepository;
    }

    @PostConstruct
    public void inicializarCache(){
        categoriaRepository.findAll().forEach(categoria -> {
            cacheCategorias.put(categoria.getNmCategoriaOriginal(), categoria.getNmCategoriaTraduzida());
        });
    }


    @Transactional
    public String buscarCategoriaTraduzida(String texto, String origem, String destino) {
        if (cacheCategorias.containsKey(texto)) {
            return cacheCategorias.get(texto);
        }

        if (CATEGORIAS_FIXAS.containsKey(texto)) {
            String categoriaFixa = CATEGORIAS_FIXAS.get(texto);
            cacheCategorias.put(texto, categoriaFixa);
            salvarBancoCategoria(texto, categoriaFixa);
            return categoriaFixa;
        }

        String categoriaTraduzida = this.traduzirAPI(texto, origem, destino);
        if (categoriaTraduzida != null && !categoriaTraduzida.isEmpty() && !categoriaTraduzida.equals(texto)) {
            cacheCategorias.put(texto, categoriaTraduzida);
            salvarBancoCategoria(texto, categoriaTraduzida);
            return categoriaTraduzida;
        }
        return texto;
    }

    @Transactional
    public void salvarBancoCategoria(String categoriaOriginal, String categoriaTraduzida){
        categoriaRepository.findByNmCategoriaOriginal(categoriaOriginal).orElseGet(() ->
                categoriaRepository.save(new Categoria(categoriaOriginal, categoriaTraduzida)));
    }

    @Transactional
    @RateLimiter(name = "traducaoService", fallbackMethod = "traducaoOffline")
    public String traduzirAPI(String texto, String origem, String destino) {
        try {
            String textoEncoded = URLEncoder.encode(texto, StandardCharsets.UTF_8);
            String urlFormatada = String.format(MYMEMORY_URL, textoEncoded, origem, destino);
            ResponseEntity<MyMemoryResponse> resposta = restTemplate.exchange(
                    urlFormatada,
                    HttpMethod.GET,
                    null,
                    MyMemoryResponse.class
            );
            if (resposta.getStatusCode() == HttpStatus.OK && resposta.getBody() != null &&
                    resposta.getBody().getResponseData() != null &&
                    resposta.getBody().getResponseData().getTranslatedText() != null &&
                    !resposta.getBody().getResponseData().getTranslatedText().isEmpty()) {
                return resposta.getBody().getResponseData().getTranslatedText();

            }
        } catch (Exception e) {
            System.out.println("Erro ao traduzir: " + e.getMessage());
        }
        return texto;
    }

    public String traducaoOffline(String texto, String origem, String destino, Throwable ex) {
        System.out.println("⏳ Fallback ativado (RateLimiter): " + ex.getMessage());
        return texto; // retorna original
    }
}*/
