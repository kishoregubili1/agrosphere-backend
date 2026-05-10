package com.agrosphere.service.impl;

import com.agrosphere.dto.request.FarmRequest;
import com.agrosphere.entity.Farm;
import com.agrosphere.entity.Tenant;
import com.agrosphere.repository.FarmRepository;
import com.agrosphere.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepository farmRepository;
    private final TenantRepository tenantRepository;

    public List<Farm> getMyFarms(Long tenantId) {
        return farmRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    public Farm getById(Long id, Long tenantId) {
        return farmRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
    }

    @Transactional
    public Farm create(FarmRequest req, Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Farm farm = Farm.builder()
                .name(req.getName())
                .location(req.getLocation())
                .district(req.getDistrict())
                .state(req.getState())
                .totalAreaAcres(req.getTotalAreaAcres())
                .description(req.getDescription())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .tenant(tenant)
                .build();
        return farmRepository.save(farm);
    }

    @Transactional
    public Farm update(Long id, FarmRequest req, Long tenantId) {
        Farm farm = getById(id, tenantId);
        farm.setName(req.getName());
        farm.setLocation(req.getLocation());
        farm.setDistrict(req.getDistrict());
        farm.setState(req.getState());
        farm.setTotalAreaAcres(req.getTotalAreaAcres());
        farm.setDescription(req.getDescription());
        return farmRepository.save(farm);
    }

    @Transactional
    public void delete(Long id, Long tenantId) {
        Farm farm = getById(id, tenantId);
        farm.setIsActive(false);
        farmRepository.save(farm);
    }
}
