package com.trocabook.Trocabook.service.impl;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        if (this.apiKey.isBlank()) {
            System.err.println("[IaService] GROQ_API_KEY não encontrada no ambiente. Usando fallback local.");
        } else {
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
                System.err.println("[IaService] Falha ao chamar Groq, acionando fallback local: " + e.getMessage());
            }
        }

        return gerarDescricaoFallbackLocal(titulo, autores, categorias, tipoNegociacao, rascunhoUsuario);
    }

    public String chamarIa(String prompt) {
        Exception ultimoErro = null;

        for (String modelo : MODELOS_GROQ) {
            try {
                Map<String, Object> body = Map.of(
                        "model", modelo,
                        "temperature", 0.7,
                        "max_tokens", 300,
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