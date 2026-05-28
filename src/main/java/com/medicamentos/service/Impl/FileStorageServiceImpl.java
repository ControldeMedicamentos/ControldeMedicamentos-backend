package com.medicamentos.service.Impl;

import com.medicamentos.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads: " + rootLocation, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        try {
            String extension = extractExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path target = rootLocation.resolve(filename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename).normalize();
    }

    @Override
    public void delete(String filename) {
        try {
            Files.deleteIfExists(load(filename));
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar el archivo", e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }
}
