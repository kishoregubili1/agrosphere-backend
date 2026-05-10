package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.*;
import com.agrosphere.repository.*;
import com.agrosphere.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/farmer/crop-logs")
@RequiredArgsConstructor
public class CropGrowthLogController {
    private final CropGrowthLogRepository logRepo;
    private final CropRepository cropRepo;
    private final TenantRepository tenantRepo;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<?> getAll() {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(logRepo.findByTenantOrderByLoggedAtDesc(t)));
    }

    @GetMapping("/crop/{cropId}")
    public ResponseEntity<?> getByCrop(@PathVariable Long cropId) {
        Crop crop = cropRepo.findById(cropId).orElseThrow();
        if (!crop.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(ApiResponse.success(logRepo.findByCropOrderByLoggedAtDesc(crop)));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Req req) {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        Crop crop = cropRepo.findById(req.cropId).orElseThrow();
        if (!crop.getTenant().getId().equals(t.getId())) return ResponseEntity.status(403).build();
        // Optionally update the crop's current growth stage + health score
        if (req.stage != null) crop.setCurrentGrowthStage(req.stage);
        if (req.healthScore != null) crop.setHealthScore(req.healthScore);
        cropRepo.save(crop);
        CropGrowthLog log = CropGrowthLog.builder()
            .tenant(t).crop(crop).stage(req.stage)
            .healthScore(req.healthScore).notes(req.notes)
            .issuesJson(req.issuesJson).photoUrl(req.photoUrl).build();
        return ResponseEntity.ok(ApiResponse.success(logRepo.save(log), "Log saved"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        CropGrowthLog log = logRepo.findById(id).orElseThrow();
        if (!log.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        logRepo.delete(log);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @Data static class Req { Long cropId; String stage, notes, issuesJson, photoUrl; Integer healthScore; }
}
