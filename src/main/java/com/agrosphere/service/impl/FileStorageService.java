package com.agrosphere.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir:/tmp/uploads}") private String uploadDir;
    @Value("${app.base-url:http://localhost:8080/api}") private String baseUrl;

    public String store(MultipartFile file, String subDir) {
        try {
            String ext = getExt(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + ext;
            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return baseUrl + "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) { throw new RuntimeException("File upload failed: " + e.getMessage()); }
    }
    private String getExt(String f) {
        if (f == null || !f.contains(".")) return "jpg";
        return f.substring(f.lastIndexOf('.')+1).toLowerCase();
    }
}
