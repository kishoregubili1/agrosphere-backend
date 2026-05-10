package com.agrosphere.service.impl;

import com.agrosphere.dto.request.DiseaseLogRequest;
import com.agrosphere.entity.*;
import com.agrosphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;
    private final CropDiseaseLogRepository diseaseLogRepository;
    private final CropRepository cropRepository;
    private final TenantRepository tenantRepository;

    public List<Disease> getAllDiseases() {
        return diseaseRepository.findByIsActiveTrue();
    }

    public List<CropDiseaseLog> getMyDiseaseLogs(Long tenantId) {
        return diseaseLogRepository.findByTenant_IdOrderByCreatedAtDesc(tenantId);
    }

    public List<CropDiseaseLog> getActiveAlerts(Long tenantId) {
        return diseaseLogRepository.findByTenant_IdAndIsResolvedFalse(tenantId);
    }

    @Transactional
    public CropDiseaseLog reportDisease(DiseaseLogRequest req, Long tenantId) {
        Crop crop = cropRepository.findByIdAndTenantId(req.getCropId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Crop not found or not yours"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Disease disease = req.getDiseaseId() != null
                ? diseaseRepository.findById(req.getDiseaseId()).orElse(null)
                : null;

        CropDiseaseLog log = diseaseLogRepository.save(CropDiseaseLog.builder()
                .crop(crop)
                .tenant(tenant)
                .disease(disease)
                .detectedDate(req.getDetectedDate() != null ? req.getDetectedDate() : LocalDate.now())
                .notes(req.getNotes())
                .severity(req.getSeverity())
                .photoUrl(req.getPhotoUrl())
                .build());

        // Reduce crop health score based on severity
        if (req.getSeverity() != null) {
            int penalty = switch (req.getSeverity()) {
                case LOW -> 5; case MEDIUM -> 15; case HIGH -> 25; case CRITICAL -> 40;
            };
            int current = crop.getHealthScore() != null ? crop.getHealthScore() : 80;
            crop.setHealthScore(Math.max(0, current - penalty));
            cropRepository.save(crop);
        }

        return log;
    }

    @Transactional
    public CropDiseaseLog resolveDisease(Long id, Long tenantId) {
        CropDiseaseLog log = diseaseLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disease log not found"));
        // Verify ownership via crop's tenant
        if (!log.getCrop().getTenant().getId().equals(tenantId))
            throw new RuntimeException("Access denied");
        log.setIsResolved(true);
        log.setResolvedDate(LocalDate.now());
        return diseaseLogRepository.save(log);
    }
}
