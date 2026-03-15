package br.edu.infnet.gestao_academica.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class StorageService {

    private final Path uploadDir;

    public StorageService(@Value("${app.storage.upload-dir:data/uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir);
    }

    public String salvar(MultipartFile arquivo) {
        try {
            Files.createDirectories(uploadDir);
            String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
            Path destino = uploadDir.resolve(nomeArquivo);
            arquivo.transferTo(destino);
            return destino.toString();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo: " + e.getMessage(), e);
        }
    }
}
