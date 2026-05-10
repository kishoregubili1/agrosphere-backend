package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.service.impl.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;
    @Value("${file.upload-dir:./uploads}") private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value="folder",defaultValue="general") String folder) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl", fileStorageService.store(file, folder))));
    }

    @GetMapping("/uploads/{folder}/{filename}")
    public ResponseEntity<Resource> serve(@PathVariable String folder, @PathVariable String filename) {
        try {
            Path p = Paths.get(uploadDir).resolve(folder).resolve(filename);
            Resource r = new UrlResource(p.toUri());
            if (!r.exists()) return ResponseEntity.notFound().build();
            String ct = filename.endsWith(".png") ? "image/png" : filename.endsWith(".gif") ? "image/gif" : "image/jpeg";
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(ct))
                .header(HttpHeaders.CACHE_CONTROL,"max-age=86400").body(r);
        } catch (Exception e) { return ResponseEntity.notFound().build(); }
    }
}
