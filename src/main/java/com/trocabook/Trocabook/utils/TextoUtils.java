package com.trocabook.Trocabook.utils;

import java.text.Normalizer;

public class TextoUtils {
    public static String normalizar(String texto) {
        if (texto == null) return null;
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim()
                .toLowerCase();
    }

    public static String garantirHttps(String url) {
        if (url == null || url.isBlank()) return url;
        if (url.startsWith("http://")) {
            return url.replaceFirst("http://", "https://");
        }
        return url;
    }

}
