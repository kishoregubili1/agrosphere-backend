package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.SoilTest;
import com.agrosphere.entity.Tenant;
import com.agrosphere.repository.SoilTestRepository;
import com.agrosphere.repository.TenantRepository;
import com.agrosphere.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/farmer/soil")
@RequiredArgsConstructor
public class SoilController {
    private final SoilTestRepository repo;
    private final TenantRepository tenantRepo;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<?> getAll() {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(repo.findByTenantOrderByTestDateDesc(t)));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Req req) {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        SoilTest st = SoilTest.builder().tenant(t).fieldName(req.fieldName)
            .soilType(req.soilType).testDate(req.testDate != null ? LocalDate.parse(req.testDate) : LocalDate.now())
            .ph(req.ph).nitrogen(req.nitrogen).phosphorus(req.phosphorus).potassium(req.potassium)
            .organicMatter(req.organicMatter)
            .zinc(req.zinc == null ? "adequate" : req.zinc)
            .boron(req.boron == null ? "adequate" : req.boron)
            .notes(req.notes).build();
        return ResponseEntity.ok(ApiResponse.success(repo.save(st), "Saved"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Req req) {
        SoilTest st = repo.findById(id).orElseThrow();
        if (!st.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        st.setFieldName(req.fieldName); st.setSoilType(req.soilType);
        if (req.testDate != null) st.setTestDate(LocalDate.parse(req.testDate));
        st.setPh(req.ph); st.setNitrogen(req.nitrogen); st.setPhosphorus(req.phosphorus);
        st.setPotassium(req.potassium); st.setOrganicMatter(req.organicMatter);
        if (req.zinc != null) st.setZinc(req.zinc);
        if (req.boron != null) st.setBoron(req.boron);
        st.setNotes(req.notes);
        return ResponseEntity.ok(ApiResponse.success(repo.save(st), "Updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        SoilTest st = repo.findById(id).orElseThrow();
        if (!st.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        repo.delete(st);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @Data static class Req {
        String fieldName, soilType, testDate, zinc, boron, notes;
        Double ph, nitrogen, phosphorus, potassium, organicMatter;
    }
}
