package com.trocabook.Trocabook.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCache anunciosCache =
                new CaffeineCache(
                        "anuncios",
                        Caffeine.newBuilder()
                                .maximumSize(500)
                                .expireAfterWrite(
                                        Duration.ofMinutes(10)
                                )
                                .build()
                );

        CaffeineCache recomendacoesCache =
                new CaffeineCache(
                        "recomendacoes",
                        Caffeine.newBuilder()
                                .maximumSize(500)
                                .expireAfterWrite(
                                        Duration.ofMinutes(80)
                                )
                                .build()
                );

        SimpleCacheManager cacheManager =
                new SimpleCacheManager();

        cacheManager.setCaches(
                List.of(
                        anunciosCache,
                        recomendacoesCache
                )
        );

        return cacheManager;
    }
}