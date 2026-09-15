package com.trocabook.Trocabook.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApplicationInstance {

    private final String id = UUID.randomUUID().toString();

    public String getId() {
        return id;
    }
}