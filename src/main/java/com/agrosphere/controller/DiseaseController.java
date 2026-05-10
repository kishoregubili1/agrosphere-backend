package com.agrosphere.controller;

import com.agrosphere.dto.request.DiseaseLogRequest;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.*;
import com.agrosphere.repository.*;
import com.agrosphere.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/farmer")
@RequiredArgsConstructor
public class DiseaseController {

    private final CropDiseaseLogRepository logRepo;
    private final CropRepository cropRepository;
    private final DiseaseRepository diseaseRepository;
    private final TenantRepository tenantRepository;
    private final AuthUtil authUtil;

    @GetMapping("/disease-logs")
    public ResponseEntity<?> getLogs() {
        Long tenantId = authUtil.getCurrentTenantId();
        List<CropDiseaseLog> logs = logRepo.findByCrop_TenantId(tenantId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @PostMapping("/disease-logs")
    public ResponseEntity<?> report(@RequestBody DiseaseLogRequest req) {
        Long tenantId = authUtil.getCurrentTenantId();
        Tenant tenant = tenantRepository.findById(tenantId).orElseThrow();

        Crop crop = cropRepository.findByIdAndTenantId(req.getCropId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        Disease disease = req.getDiseaseId() != null
                ? diseaseRepository.findById(req.getDiseaseId()).orElse(null)
                : null;

        CropDiseaseLog log = CropDiseaseLog.builder()
                .crop(crop)
                .tenant(tenant)
                .disease(disease)
                .detectedDate(req.getDetectedDate() != null ? req.getDetectedDate() : LocalDate.now())
                .notes(req.getNotes())
                .severity(req.getSeverity())
                .photoUrl(req.getPhotoUrl())
                .build();

        // Reduce crop health score
        if (req.getSeverity() != null) {
            int penalty = switch (req.getSeverity()) {
                case LOW -> 5; case MEDIUM -> 15; case HIGH -> 25; case CRITICAL -> 40;
            };
            int current = crop.getHealthScore() != null ? crop.getHealthScore() : 80;
            crop.setHealthScore(Math.max(0, current - penalty));
            cropRepository.save(crop);
        }

        return ResponseEntity.ok(ApiResponse.success(logRepo.save(log), "Disease reported"));
    }

    @PatchMapping("/disease-logs/{id}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Long id) {
        CropDiseaseLog log = logRepo.findById(id).orElseThrow();
        log.setIsResolved(true);
        log.setResolvedDate(LocalDate.now());
        logRepo.save(log);
        return ResponseEntity.ok(ApiResponse.success(null, "Resolved"));
    }
}
