package com.trocabook.Trocabook.service.impl;

import com.trocabook.Trocabook.service.ITraducaoService;
import com.trocabook.Trocabook.service.feign.MyMemoryAPIService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TraducaoService implements ITraducaoService {
    private static final String LINGUA_TRADUCAO = "pt-BR";

    private final MyMemoryAPIService myMemoryAPIService;

    public TraducaoService(MyMemoryAPIService myMemoryAPIService) {
        this.myMemoryAPIService = myMemoryAPIService;
    }

    @Override
    public String traduzirTituloECategorias(
            String titulo,
            List<String> categorias,
            String linguaOriginal
    ) {
        String texto = titulo
                + " ||| "
                + String.join(" ### ", categorias);

        try {
            return myMemoryAPIService
                    .traduzir(texto, linguaOriginal + "|" + LINGUA_TRADUCAO)
                    .responseData()
                    .translatedText();
        } catch (Exception e) {
            System.err.println("[TraducaoService] Falha ao traduzir via MyMemory API: " + e.getMessage());
            return texto;
        }
    }
}