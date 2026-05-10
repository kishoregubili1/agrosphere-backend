package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.SeasonPlan;
import com.agrosphere.entity.Tenant;
import com.agrosphere.repository.SeasonPlanRepository;
import com.agrosphere.repository.TenantRepository;
import com.agrosphere.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/farmer/season-plans")
@RequiredArgsConstructor
public class SeasonPlanController {
    private final SeasonPlanRepository repo;
    private final TenantRepository tenantRepo;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<?> getAll() {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(repo.findByTenantOrderByYearDescSeasonAsc(t)));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Req req) {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        SeasonPlan p = SeasonPlan.builder().tenant(t).season(req.season)
            .year(req.year).cropsJson(req.cropsJson).notes(req.notes)
            .status(req.status == null ? "planned" : req.status).build();
        return ResponseEntity.ok(ApiResponse.success(repo.save(p), "Plan saved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Req req) {
        SeasonPlan p = repo.findById(id).orElseThrow();
        if (!p.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        p.setSeason(req.season); p.setYear(req.year); p.setCropsJson(req.cropsJson);
        p.setNotes(req.notes);
        if (req.status != null) p.setStatus(req.status);
        return ResponseEntity.ok(ApiResponse.success(repo.save(p), "Updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        SeasonPlan p = repo.findById(id).orElseThrow();
        if (!p.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        repo.delete(p);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @Data static class Req { String season, cropsJson, notes, status; Integer year; }
}
