package com.agrosphere.service.impl;

import com.agrosphere.dto.request.FieldRequest;
import com.agrosphere.entity.*;
import com.agrosphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;
    private final TenantRepository tenantRepository;

    public List<Field> getByFarm(Long farmId, Long tenantId) {
        return fieldRepository.findByFarmIdAndTenantId(farmId, tenantId);
    }

    public List<Field> getAllMyFields(Long tenantId) {
        return fieldRepository.findByTenantId(tenantId);
    }

    @Transactional
    public Field create(FieldRequest req, Long tenantId) {
        Farm farm = farmRepository.findByIdAndTenantId(req.getFarmId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return fieldRepository.save(Field.builder()
                .name(req.getName())
                .areaAcres(req.getAreaAcres())
                .soilType(req.getSoilType())
                .description(req.getDescription())
                .farm(farm)
                .tenant(tenant)
                .build());
    }

    @Transactional
    public Field update(Long id, FieldRequest req, Long tenantId) {
        Field field = fieldRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
        field.setName(req.getName());
        field.setAreaAcres(req.getAreaAcres());
        field.setSoilType(req.getSoilType());
        field.setDescription(req.getDescription());
        return fieldRepository.save(field);
    }

    @Transactional
    public void delete(Long id, Long tenantId) {
        Field field = fieldRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
        fieldRepository.delete(field);
    }
}
