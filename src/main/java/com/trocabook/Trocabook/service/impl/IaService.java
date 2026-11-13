package com.trocabook.Trocabook.service.impl;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class IaService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final List<String> MODELOS_GROQ = List.of(
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "qwen/qwen3.8-27b"
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "o", "as", "os", "um", "uma", "uns", "umas", "de", "do", "da", "dos", "das",
            "em", "no", "na", "nos", "nas", "por", "para", "com", "sem", "sob", "sobre",
            "e", "ou", "mas", "que", "qual", "quais", "quero", "queria", "busco", "procuro",
            "livro", "livros", "obra", "obras", "historia", "história", "algo", "algum", "alguma",
            "parecido", "parecidos", "tipo", "estilo", "ler", "leitura", "bom", "boa", "muito"
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public IaService(
            @Value("${groq.api.key:}") String apiKey,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public String gerarDescricaoAnuncio(
            String titulo,
            String autores,
            String categorias,
            String tipoNegociacao,
            String rascunhoUsuario
    ) {
        if (!this.apiKey.isBlank()) {
            try {
                String detalhesExtras = (rascunhoUsuario != null && !rascunhoUsuario.isBlank())
                        ? "Observações informadas pelo dono do livro (incorpore isso naturalmente): " + rascunhoUsuario
                        : "O dono não informou detalhes físicos específicos, então foque na obra e incentive interessados a chamarem no chat para saber mais sobre o estado do exemplar.";

                String prompt = String.format("""
                        Você é um assistente da plataforma Trocabook (site brasileiro de troca e venda de livros usados entre leitores).
                        Escreva uma descrição curta, acolhedora e persuasiva em português do Brasil para um anúncio de livro.
                        
                        Dados do livro:
                        - Título: %s
                        - Autor(es): %s
                        - Categorias: %s
                        - Modalidade: %s
                        - %s
                        
                        Regras:
                        - Máximo de 2 parágrafos curtos (até 380 caracteres no total).
                        - Traduza categorias em inglês para português naturalmente (ex: Fiction -> Ficção).
                        - Não use markdown (sem asteriscos **, sem hashtags # e sem listas com hífen).
                        - Não invente estado de conservação ou detalhes que não foram informados.
                        - Retorne apenas o texto pronto da descrição.
                        """,
                        titulo,
                        (autores != null && !autores.isBlank()) ? autores : "Não informado",
                        (categorias != null && !categorias.isBlank()) ? categorias : "Geral",
                        (tipoNegociacao != null && !tipoNegociacao.isBlank()) ? tipoNegociacao : "Troca ou Venda",
                        detalhesExtras
                );

                return chamarIa(prompt);
            } catch (Exception e) {
                System.err.println("[IaService] Falha ao chamar Groq na descrição, acionando fallback: " + e.getMessage());
            }
        }

        return gerarDescricaoFallbackLocal(titulo, autores, categorias, tipoNegociacao, rascunhoUsuario);
    }

    /**
     * Interpreta uma descrição ou consulta livre na tela de anúncio e retorna até 3 títulos reais de livros.
     */
    public List<String> identificarTitulosParaAnuncio(String consultaUsuario) {
        if (consultaUsuario == null || consultaUsuario.isBlank()) {
            return List.of();
        }

        if (!this.apiKey.isBlank()) {
            try {
                String prompt = String.format("""
                        O usuário está cadastrando um livro na plataforma Trocabook e digitou: "%s".
                        Se for um título exato, retorne esse título.
                        Se for uma descrição de enredo, tema, autor ou pista sobre um livro, identifique até 3 títulos reais de livros que correspondem exatamente ao que foi descrito.
                        
                        Regras obrigatórias:
                        - Retorne APENAS os títulos dos livros, separados por ponto e vírgula (;).
                        - Não inclua numeração, nomes de autores, aspas, explicações ou textos extras.
                        - Exemplo de saída válida: Dom Casmurro; Memórias Póstumas de Brás Cubas; Quincas Borba
                        """, consultaUsuario.trim());

                String resposta = chamarIa(prompt);
                List<String> titulos = Arrays.stream(resposta.split("[;\\n]+"))
                        .map(t -> t.replaceAll("^[-*\\d.\\s\"']+|[\"']+$", "").trim())
                        .filter(t -> !t.isBlank() && t.length() <= 120)
                        .distinct()
                        .limit(3)
                        .toList();

                if (!titulos.isEmpty()) {
                    return titulos;
                }
            } catch (Exception e) {
                System.err.println("[IaService] Falha ao identificar títulos via Groq: " + e.getMessage());
            }
        }

        return List.of(consultaUsuario.trim());
    }

    /**
     * Expande uma consulta em linguagem natural para palavras-chave, temas, gêneros e autores relacionados.
     */
    public List<String> extrairTermosBuscaSemantica(String consultaUsuario) {
        if (consultaUsuario == null || consultaUsuario.isBlank()) {
            return List.of();
        }

        Set<String> termosFinais = new LinkedHashSet<>(extrairTokensLocais(consultaUsuario));

        if (!this.apiKey.isBlank()) {
            try {
                String prompt = String.format("""
                        Você é o motor de busca semântica do Trocabook (plataforma brasileira de troca e venda de livros).
                        Um usuário pesquisou: "%s"
                        
                        Gere de 5 a 10 palavras-chave curtas (gêneros em português e inglês, temas, sinônimos, autores ou títulos relacionados) para encontrar livros compatíveis no catálogo.
                        
                        Regras obrigatórias:
                        - Retorne APENAS as palavras-chave separadas por vírgula.
                        - Sem frases, sem numeração, sem explicações.
                        - Exemplo: suspense, thriller, mistério, investigação, ficção, fiction, Agatha Christie
                        """, consultaUsuario.trim());

                String resposta = chamarIa(prompt);
                Arrays.stream(resposta.split("[,;\\n]+"))
                        .map(this::normalizarTexto)
                        .filter(t -> t.length() >= 2 && t.length() <= 50)
                        .limit(12)
                        .forEach(termosFinais::add);
            } catch (Exception e) {
                System.err.println("[IaService] Falha na expansão semântica via Groq, usando tokens locais: " + e.getMessage());
            }
        }

        return List.copyOf(termosFinais);
    }

    public List<String> extrairTokensLocais(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        String normalizado = normalizarTexto(texto);
        return Arrays.stream(normalizado.split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 2 && !STOP_WORDS.contains(token))
                .distinct()
                .toList();
    }

    public String normalizarTexto(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    public String chamarIa(String prompt) {
        Exception ultimoErro = null;

        for (String modelo : MODELOS_GROQ) {
            try {
                Map<String, Object> body = Map.of(
                        "model", modelo,
                        "temperature", 0.4,
                        "max_tokens", 250,
                        "messages", List.of(
                                Map.of("role", "user", "content", prompt)
                        )
                );

                String rawResponse = restClient.post()
                        .uri(GROQ_URL)
                        .header("Authorization", "Bearer " + this.apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);

                JsonNode response = objectMapper.readTree(rawResponse);
                String texto = response
                        .path("choices")
                        .get(0)
                        .path("message")
                        .path("content")
                        .asText("");

                if (!texto.isBlank()) {
                    return texto.trim();
                }
            } catch (Exception e) {
                ultimoErro = e;
            }
        }

        throw new RuntimeException("Falha ao consultar modelos Groq.", ultimoErro);
    }

    private String gerarDescricaoFallbackLocal(
            String titulo,
            String autores,
            String categorias,
            String tipoNegociacao,
            String rascunhoUsuario
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("Estou disponibilizando o livro \"").append(titulo.trim()).append("\"");
        if (autores != null && !autores.isBlank()) {
            sb.append(", de ").append(autores.trim());
        }
        sb.append(".");

        if (categorias != null && !categorias.isBlank()) {
            String catFormatada = categorias.trim().replaceAll("(?i)\\bFiction\\b", "Ficção");
            sb.append(" Ótima pedida para quem curte ").append(catFormatada).append(".");
        }

        if (rascunhoUsuario != null && !rascunhoUsuario.isBlank()) {
            String obs = rascunhoUsuario.trim();
            if (!obs.endsWith(".") && !obs.endsWith("!")) {
                obs += ".";
            }
            sb.append("\n\n").append(obs).append(" ");
        } else {
            sb.append("\n\n");
        }

        String modalidade = tipoNegociacao != null ? tipoNegociacao.toUpperCase(Locale.ROOT) : "";
        switch (modalidade) {
            case "TROCA" -> sb.append("Disponível para troca no Trocabook. Chame no chat para saber mais sobre o exemplar e combinar!");
            case "VENDA" -> sb.append("Disponível para venda no Trocabook. Chame no chat para conferir o estado do livro e combinar!");
            case "AMBOS" -> sb.append("Disponível para troca ou venda no Trocabook. Envie sua proposta pelo chat!");
            default -> sb.append("Quer saber mais sobre o estado de conservação ou fazer uma proposta? Chame no chat!");
        }

        return sb.toString().trim();
    }
}