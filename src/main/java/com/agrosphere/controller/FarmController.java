package com.agrosphere.controller;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.Farm;
import com.agrosphere.entity.Tenant;
import com.agrosphere.repository.FarmRepository;
import com.agrosphere.repository.TenantRepository;
import com.agrosphere.service.impl.FileStorageService;
import com.agrosphere.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController @RequestMapping("/farmer") @RequiredArgsConstructor
public class FarmController {
    private final FarmRepository farmRepository;
    private final TenantRepository tenantRepository;
    private final FileStorageService fileStorageService;
    private final AuthUtil authUtil;

    @GetMapping("/farms") public ResponseEntity<?> getFarms() {
        return ResponseEntity.ok(ApiResponse.success(farmRepository.findByTenantId(authUtil.getCurrentTenantId())));
    }
    @PostMapping("/farms") public ResponseEntity<?> create(@RequestBody Farm req) {
        Tenant t = tenantRepository.findById(authUtil.getCurrentTenantId()).orElseThrow();
        req.setTenant(t);
        return ResponseEntity.ok(ApiResponse.success(farmRepository.save(req), "Farm added"));
    }
    @PutMapping("/farms/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Farm req) {
        Farm f = farmRepository.findById(id).orElseThrow();
        f.setName(req.getName()); f.setLocation(req.getLocation()); f.setDistrict(req.getDistrict());
        f.setState(req.getState()); f.setTotalAreaAcres(req.getTotalAreaAcres()); f.setDescription(req.getDescription());
        if (req.getImageUrl() != null) f.setImageUrl(req.getImageUrl());
        return ResponseEntity.ok(ApiResponse.success(farmRepository.save(f)));
    }
    @DeleteMapping("/farms/{id}") public ResponseEntity<?> delete(@PathVariable Long id) {
        farmRepository.deleteById(id); return ResponseEntity.ok(ApiResponse.success(null,"Deleted"));
    }
    @PostMapping("/farms/{id}/image") public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Farm f = farmRepository.findById(id).orElseThrow();
        String url = fileStorageService.store(file,"farms");
        f.setImageUrl(url); farmRepository.save(f);
        return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl",url)));
    }
}
