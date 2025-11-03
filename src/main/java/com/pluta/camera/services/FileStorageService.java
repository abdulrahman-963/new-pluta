package com.pluta.camera.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    @Value("${analysis.upload-dir}")
    private String uploadDir;

    public String saveFile(File file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = System.currentTimeMillis() + "_" + file.getName();
        Path filePath = uploadPath.resolve(filename);

        // Save file
        Files.copy(file.toPath(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            // Log error but don't fail
        }
    }
}