package com.trocabook.Trocabook.adapter;

import com.trocabook.Trocabook.model.dto.GoogleBooksResponse;
import com.trocabook.Trocabook.model.dto.LivroBuscaOutput;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoogleAPIBooksAdapter {

    public List<LivroBuscaOutput> adaptar(GoogleBooksResponse response) {

        return response.items()
                .stream()
                .map(this::adaptarItem)
                .toList();
    }

    private LivroBuscaOutput adaptarItem(
            GoogleBooksResponse.Items item) {

        return new LivroBuscaOutput(
                item.id(),
                item.volumeInfo().title(),
                item.volumeInfo().authors() == null
                        ? List.of()
                        : item.volumeInfo().authors(),
                item.volumeInfo().publisher(),
                item.volumeInfo().publishedDate(),
                item.volumeInfo().imageLinks() == null
                        ? null
                        : garantirHttps(item.volumeInfo().imageLinks().thumbnail()),
                item.volumeInfo().language() == null
                        ? "en"
                        : item.volumeInfo().language(),
                item.volumeInfo().categories() == null
                        ? List.of()
                        : item.volumeInfo().categories()
        );
    }

    private String garantirHttps(String url){
        return url != null && url.startsWith("http:") ? new StringBuilder(url).replace(0, 5, "https:").toString() : url;
    }
}