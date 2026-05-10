package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.*;
import com.agrosphere.enums.Role;
import com.agrosphere.repository.*;
import com.agrosphere.service.impl.FileStorageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CropRepository cropRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CropTypeRepository cropTypeRepository;
    private final DiseaseRepository diseaseRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    // ─── STATS ───────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getPlatformStats() {
        long farmers = userRepository.findByRole(Role.FARMER).size();
        long consumers = userRepository.findByRole(Role.CONSUMER).size();
        long crops = cropRepository.count();
        long orders = orderRepository.countAllOrders();
        BigDecimal revenue = orderRepository.sumPlatformRevenue();
        long products = productRepository.findByIsActiveTrue().size();
        return ResponseEntity.ok(ApiResponse.success(
            new PlatformStats(farmers, consumers, crops, orders, revenue, products)));
    }

    // ─── FARMER CRUD ─────────────────────────────────────
    @GetMapping("/farmers")
    public ResponseEntity<?> getAllFarmers() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findByRoleWithTenant(Role.FARMER)));
    }

    @GetMapping("/farmers/{id}")
    public ResponseEntity<?> getFarmer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
            userRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"))));
    }

    @PostMapping("/farmers")
    public ResponseEntity<?> createFarmer(@RequestBody CreateFarmerRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already exists");

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name(req.getFarmName() != null ? req.getFarmName() : req.getName() + "'s Farm")
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .district(req.getDistrict())
                .state(req.getState())
                .build());

        User user = userRepository.save(User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phoneNumber(req.getPhoneNumber())
                .role(Role.FARMER)
                .tenant(tenant)
                .build());

        return ResponseEntity.ok(ApiResponse.success(user, "Farmer created. They can now login."));
    }

    @PutMapping("/farmers/{id}")
    public ResponseEntity<?> updateFarmer(@PathVariable Long id, @RequestBody UpdateFarmerRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (req.getName() != null) user.setName(req.getName());
        if (req.getPhoneNumber() != null) user.setPhoneNumber(req.getPhoneNumber());
        if (req.getPassword() != null && !req.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (user.getTenant() != null) {
            Tenant t = user.getTenant();
            if (req.getDistrict() != null) t.setDistrict(req.getDistrict());
            if (req.getState() != null) t.setState(req.getState());
            if (req.getFarmName() != null) t.setName(req.getFarmName());
            tenantRepository.save(t);
        }
        return ResponseEntity.ok(ApiResponse.success(userRepository.save(user), "Updated"));
    }

    @DeleteMapping("/farmers/{id}")
    public ResponseEntity<?> deleteFarmer(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Farmer deleted"));
    }

    @PatchMapping("/farmers/{id}/toggle")
    public ResponseEntity<?> toggleFarmerStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(null,
                user.getIsActive() ? "Farmer activated" : "Farmer deactivated"));
    }

    @PatchMapping("/farmers/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElseThrow();
        user.setPassword(passwordEncoder.encode(body.get("newPassword")));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }

    // ─── CONSUMERS ───────────────────────────────────────
    @GetMapping("/consumers")
    public ResponseEntity<?> getAllConsumers() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findByRole(Role.CONSUMER)));
    }

    @PatchMapping("/consumers/{id}/toggle")
    public ResponseEntity<?> toggleConsumer(@PathVariable Long id) {
        User u = userRepository.findById(id).orElseThrow();
        u.setIsActive(!u.getIsActive());
        userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.success(null, u.getIsActive() ? "Activated" : "Deactivated"));
    }

    // ─── ORDERS ──────────────────────────────────────────
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(orderRepository.findAll()));
    }

    // ─── PRODUCTS ────────────────────────────────────────
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productRepository.findAll()));
    }

    @PatchMapping("/products/{id}/toggle")
    public ResponseEntity<?> toggleProduct(@PathVariable Long id) {
        Product p = productRepository.findById(id).orElseThrow();
        p.setIsActive(!p.getIsActive());
        productRepository.save(p);
        return ResponseEntity.ok(ApiResponse.success(null, "Product visibility updated"));
    }

    // ─── CROP TYPES ──────────────────────────────────────
    @GetMapping("/crop-types")
    public ResponseEntity<?> getCropTypes() {
        return ResponseEntity.ok(ApiResponse.success(cropTypeRepository.findAll()));
    }

    @PostMapping("/crop-types")
    public ResponseEntity<?> createCropType(@RequestBody CropType ct) {
        return ResponseEntity.ok(ApiResponse.success(cropTypeRepository.save(ct), "CropType created"));
    }

    @PutMapping("/crop-types/{id}")
    public ResponseEntity<?> updateCropType(@PathVariable Long id, @RequestBody CropType updated) {
        CropType ct = cropTypeRepository.findById(id).orElseThrow();
        ct.setName(updated.getName()); ct.setCategory(updated.getCategory());
        ct.setSeasonType(updated.getSeasonType()); ct.setAvgDurationDays(updated.getAvgDurationDays());
        ct.setIconEmoji(updated.getIconEmoji()); ct.setDescription(updated.getDescription());
        if (updated.getImageUrl() != null) ct.setImageUrl(updated.getImageUrl());
        return ResponseEntity.ok(ApiResponse.success(cropTypeRepository.save(ct)));
    }

    @DeleteMapping("/crop-types/{id}")
    public ResponseEntity<?> deleteCropType(@PathVariable Long id) {
        cropTypeRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @PostMapping("/crop-types/{id}/image")
    public ResponseEntity<?> uploadCropTypeImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        CropType ct = cropTypeRepository.findById(id).orElseThrow();
        String url = fileStorageService.store(file, "crop-types");
        ct.setImageUrl(url);
        cropTypeRepository.save(ct);
        return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl", url)));
    }

    // ─── DISEASES ────────────────────────────────────────
    @GetMapping("/diseases")
    public ResponseEntity<?> getDiseases() {
        return ResponseEntity.ok(ApiResponse.success(diseaseRepository.findAll()));
    }

    @PostMapping("/diseases")
    public ResponseEntity<?> createDisease(@RequestBody Disease d) {
        return ResponseEntity.ok(ApiResponse.success(diseaseRepository.save(d), "Disease added"));
    }

    @PutMapping("/diseases/{id}")
    public ResponseEntity<?> updateDisease(@PathVariable Long id, @RequestBody Disease upd) {
        Disease d = diseaseRepository.findById(id).orElseThrow();
        d.setName(upd.getName()); d.setSymptoms(upd.getSymptoms());
        d.setTreatment(upd.getTreatment()); d.setPrevention(upd.getPrevention());
        d.setSeverity(upd.getSeverity()); d.setAffectedCrops(upd.getAffectedCrops());
        if (upd.getImageUrl() != null) d.setImageUrl(upd.getImageUrl());
        return ResponseEntity.ok(ApiResponse.success(diseaseRepository.save(d)));
    }

    @DeleteMapping("/diseases/{id}")
    public ResponseEntity<?> deleteDisease(@PathVariable Long id) {
        diseaseRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @PostMapping("/diseases/{id}/image")
    public ResponseEntity<?> uploadDiseaseImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Disease d = diseaseRepository.findById(id).orElseThrow();
        String url = fileStorageService.store(file, "diseases");
        d.setImageUrl(url);
        diseaseRepository.save(d);
        return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl", url)));
    }

    // ─── DTOs ────────────────────────────────────────────
    @Data static class CreateFarmerRequest {
        private String name, email, password, phoneNumber, district, state, farmName;
    }
    @Data static class UpdateFarmerRequest {
        private String name, phoneNumber, password, district, state, farmName;
    }
    @Data static class PlatformStats {
        private final long totalFarmers, totalConsumers, totalCrops, totalOrders;
        private final BigDecimal totalRevenue;
        private final long totalProducts;
    }
}
