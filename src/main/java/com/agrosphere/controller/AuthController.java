package com.agrosphere.controller;
import com.agrosphere.dto.request.LoginRequest;
import com.agrosphere.dto.request.RegisterRequest;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/login") public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(req), "Login successful"));
    }
    @PostMapping("/register/farmer") public ResponseEntity<?> regFarmer(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerFarmer(req), "Farmer registered"));
    }
    @PostMapping("/register/consumer") public ResponseEntity<?> regConsumer(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success(authService.registerConsumer(req), "Consumer registered"));
    }
}
