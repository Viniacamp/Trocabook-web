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

        System.out.println(texto);
        System.out.println(linguaOriginal);
        return myMemoryAPIService
                .traduzir(texto,
                        linguaOriginal + "|" + LINGUA_TRADUCAO)
                .responseData()
                .translatedText();
    }
}
