package com.agrosphere.controller;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.*;
import com.agrosphere.repository.*;
import com.agrosphere.service.impl.FileStorageService;
import com.agrosphere.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController @RequestMapping("/farmer") @RequiredArgsConstructor
public class CropController {
    private final CropRepository cropRepository;
    private final TenantRepository tenantRepository;
    private final CropTypeRepository cropTypeRepository;
    private final FileStorageService fileStorageService;
    private final AuthUtil authUtil;
    private final CropActivityRepository cropActivityRepository;
    private final CropDiseaseLogRepository cropDiseaseLogRepository;
    private final FinanceTransactionRepository financeTransactionRepository;
    private final ProductRepository productRepository;

    @GetMapping("/crops") public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.success(cropRepository.findByTenantId(authUtil.getCurrentTenantId())));
    }
    @PostMapping("/crops") public ResponseEntity<?> create(@RequestBody Crop req) {
        Tenant t = tenantRepository.findById(authUtil.getCurrentTenantId()).orElseThrow();
        req.setTenant(t);
        return ResponseEntity.ok(ApiResponse.success(cropRepository.save(req)));
    }
    @PutMapping("/crops/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Crop req) {
        Crop c = cropRepository.findById(id).orElseThrow();
        c.setName(req.getName()); c.setVariety(req.getVariety()); c.setAreaAcres(req.getAreaAcres());
        c.setPlantingDate(req.getPlantingDate()); c.setExpectedHarvestDate(req.getExpectedHarvestDate());
        c.setCurrentGrowthStage(req.getCurrentGrowthStage()); c.setHealthScore(req.getHealthScore());
        if (req.getImageUrl() != null) c.setImageUrl(req.getImageUrl());
        return ResponseEntity.ok(ApiResponse.success(cropRepository.save(c)));
    }
    @DeleteMapping("/crops/{id}") @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Crop crop = cropRepository.findById(id).orElseThrow();
        cropActivityRepository.deleteByCropId(id);
        cropDiseaseLogRepository.deleteByCropId(id);
        financeTransactionRepository.deleteByCropId(id);
        productRepository.deleteByCropId(id);
        cropRepository.delete(crop);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
    @PostMapping("/crops/{id}/image") public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Crop c = cropRepository.findById(id).orElseThrow();
        String url = fileStorageService.store(file,"crops");
        c.setImageUrl(url); cropRepository.save(c);
        return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl",url)));
    }
}