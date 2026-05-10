package com.agrosphere.service.impl;

import com.agrosphere.dto.request.CropRequest;
import com.agrosphere.entity.*;
import com.agrosphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;
    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;
    private final CropTypeRepository cropTypeRepository;
    private final TenantRepository tenantRepository;
    private final ActivityTemplateRepository activityTemplateRepository;
    private final CropActivityRepository cropActivityRepository;

    public List<Crop> getMyActiveCrops(Long tenantId) {
        return cropRepository.findByTenantId(tenantId);
    }

    public List<Crop> getCropsByField(Long fieldId, Long tenantId) {
        return cropRepository.findByFieldIdAndTenantId(fieldId, tenantId);
    }

    public Crop getById(Long id, Long tenantId) {
        return cropRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Crop not found"));
    }

    @Transactional
    public Crop create(CropRequest req, Long tenantId) {
        CropType cropType = cropTypeRepository.findById(req.getCropTypeId())
                .orElseThrow(() -> new RuntimeException("CropType not found"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        // Resolve field - from fieldId or farmId
        Field field = null;
        if (req.getFieldId() != null) {
            field = fieldRepository.findByIdAndTenantId(req.getFieldId(), tenantId).orElse(null);
        } else if (req.getFarmId() != null) {
            // Auto-create/find a default field for the farm
            List<Field> fields = fieldRepository.findByFarmIdAndTenantId(req.getFarmId(), tenantId);
            if (!fields.isEmpty()) {
                field = fields.get(0);
            } else {
                // Create a default field
                Farm farm = farmRepository.findByIdAndTenantId(req.getFarmId(), tenantId).orElse(null);
                if (farm != null) {
                    Field newField = Field.builder()
                            .name(req.getFieldName() != null ? req.getFieldName() : "Main Field")
                            .farm(farm)
                            .tenant(tenant)
                            .areaAcres(req.getAreaAcres())
                            .build();
                    field = fieldRepository.save(newField);
                }
            }
        }

        // Resolve dates
        LocalDate plantingDate = req.getPlantingDate() != null ? req.getPlantingDate()
                : (req.getPlantedDate() != null ? req.getPlantedDate() : LocalDate.now());
        String growthStage = req.getGrowthStage() != null ? req.getGrowthStage()
                : (req.getCurrentGrowthStage() != null ? req.getCurrentGrowthStage() : "SEEDING");

        // Auto-generate name if not provided
        String cropTypeName = cropType.getName() != null ? cropType.getName() : "Crop";
        String name = (req.getName() != null && !req.getName().isBlank())
                ? req.getName() : cropTypeName;
        // Final safety fallback - name must never be null
        if (name == null || name.isBlank()) name = "Crop-" + System.currentTimeMillis();

        Crop crop = Crop.builder()
                .name(name)
                .variety(req.getVariety())
                .cropType(cropType)
                .field(field)
                .tenant(tenant)
                .plantingDate(plantingDate)
                .expectedHarvestDate(req.getExpectedHarvestDate())
                .areaAcres(req.getAreaAcres())
                .currentGrowthStage(growthStage)
                .healthScore(req.getHealthScore() != null ? req.getHealthScore() : 80)
                .notes(req.getNotes())
                .build();

        crop = cropRepository.save(crop);
        autoGenerateActivities(crop, cropType, tenant);
        return crop;
    }

    private void autoGenerateActivities(Crop crop, CropType cropType, Tenant tenant) {
        List<ActivityTemplate> templates =
                activityTemplateRepository.findByCropTypeIdAndIsActiveTrue(cropType.getId());
        LocalDate plantedDate = crop.getPlantingDate() != null ? crop.getPlantingDate() : LocalDate.now();

        for (ActivityTemplate template : templates) {
            LocalDate scheduledDate = template.getDayFromPlanting() != null
                    ? plantedDate.plusDays(template.getDayFromPlanting())
                    : plantedDate.plusDays(7);
            String title = template.getName() != null ? template.getName() : template.getTitle();
            cropActivityRepository.save(CropActivity.builder()
                    .title(title)
                    .activityType(template.getActivityType())
                    .notes(template.getDescription())
                    .scheduledDate(scheduledDate)
                    .crop(crop)
                    .tenant(tenant)
                    .build());
        }
    }

    @Transactional
    public Crop update(Long id, CropRequest req, Long tenantId) {
        Crop crop = getById(id, tenantId);
        if (req.getName() != null && !req.getName().isBlank()) crop.setName(req.getName());
        LocalDate plantingDate = req.getPlantingDate() != null ? req.getPlantingDate() : req.getPlantedDate();
        if (plantingDate != null) crop.setPlantingDate(plantingDate);
        if (req.getExpectedHarvestDate() != null) crop.setExpectedHarvestDate(req.getExpectedHarvestDate());
        if (req.getAreaAcres() != null) crop.setAreaAcres(req.getAreaAcres());
        String growthStage = req.getGrowthStage() != null ? req.getGrowthStage() : req.getCurrentGrowthStage();
        if (growthStage != null) crop.setCurrentGrowthStage(growthStage);
        if (req.getHealthScore() != null) crop.setHealthScore(req.getHealthScore());
        if (req.getNotes() != null) crop.setNotes(req.getNotes());
        return cropRepository.save(crop);
    }

    @Transactional
    public void delete(Long id, Long tenantId) {
        Crop crop = getById(id, tenantId);
        cropRepository.delete(crop);
    }
}
