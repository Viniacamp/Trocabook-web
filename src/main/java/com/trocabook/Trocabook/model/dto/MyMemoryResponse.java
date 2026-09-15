package com.trocabook.Trocabook.model.dto;

public record MyMemoryResponse(ResponseData responseData) {

    public record ResponseData(String translatedText) {

    }
}