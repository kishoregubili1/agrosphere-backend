package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.User;
import com.agrosphere.repository.UserRepository;
import com.agrosphere.service.impl.FileStorageService;
import com.agrosphere.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(ApiResponse.success(authUtil.getCurrentUser()));
    }
    @PutMapping
    public ResponseEntity<?> update(@RequestBody Req req) {
        User u = authUtil.getCurrentUser();
        if (req.name != null) u.setName(req.name);
        if (req.phoneNumber != null) u.setPhoneNumber(req.phoneNumber);
        return ResponseEntity.ok(ApiResponse.success(userRepository.save(u),"Updated"));
    }
    @PostMapping("/picture")
    public ResponseEntity<?> picture(@RequestParam("file") MultipartFile file) {
        User u = authUtil.getCurrentUser();
        String url = fileStorageService.store(file,"profiles");
        u.setProfileImage(url);
        userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl",url)));
    }
    @Data static class Req { String name,phoneNumber; }
}
