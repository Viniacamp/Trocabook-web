package com.trocabook.Trocabook.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class FileStorageServiceUsuario {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path uploadsUsuario;
    private Path uploadsLivro;

    @PostConstruct
    public void init() {
        this.uploadsUsuario = Paths.get(uploadDir, "usuario");
        this.uploadsLivro = Paths.get(uploadDir, "livro");

        try {
            Files.createDirectories(uploadsUsuario);
            Files.createDirectories(uploadsLivro);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar os diretórios de upload.", e);
        }
    }

    public String armazenarArquivo(MultipartFile arquivo, Path diretorio, String tipo) throws IOException {
        String nomeOriginal = arquivo.getOriginalFilename();
        String nomeSeguro = nomeOriginal.replaceAll("[^a-zA-Z0-9.\\-]", "_");
        String nomeUnico = UUID.randomUUID().toString() + "_" + nomeSeguro;

        Path caminhoDestino = diretorio.resolve(nomeUnico);

        Files.copy(arquivo.getInputStream(), caminhoDestino);

        return "http://localhost:8080/uploads/" + tipo + "/" + nomeUnico;
    }

    public String armazenarArquivoUsuario(MultipartFile arquivo) throws IOException {
        return armazenarArquivo(arquivo, uploadsUsuario, "usuario");
    }

    public String armazenarArquivoLivro(MultipartFile arquivo) throws IOException {
        return armazenarArquivo(arquivo, uploadsLivro, "livro");
    }

    public void excluirArquivo(String nomeArquivo, String tipo) throws IOException {
        Path diretorioBase = tipo.equals("usuario") ? uploadsUsuario : uploadsLivro;
        Path caminhoArquivo = diretorioBase.resolve(nomeArquivo);

        if (Files.exists(caminhoArquivo)) {
            Files.delete(caminhoArquivo);
        }
    }

    public Resource carregarArquivoComoRecurso(String nomeArquivo, String tipo) {
        try {
            Path diretorioBase = tipo.equalsIgnoreCase("usuario") ? this.uploadsUsuario : this.uploadsLivro;
            Path caminhoArquivo = diretorioBase.resolve(nomeArquivo).normalize();
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Arquivo não encontrado: " + nomeArquivo);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Arquivo não encontrado: " + nomeArquivo, ex);
        }
    }
}