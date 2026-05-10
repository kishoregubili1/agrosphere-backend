package com.agrosphere.controller;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.Field;
import com.agrosphere.entity.Farm;
import com.agrosphere.repository.FieldRepository;
import com.agrosphere.repository.FarmRepository;
import com.agrosphere.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/farmer") @RequiredArgsConstructor
public class FieldController {
    private final FieldRepository fieldRepository;
    private final FarmRepository  farmRepository;
    private final AuthUtil authUtil;

    // GET /farmer/fields          — all fields for tenant
    // GET /farmer/fields?farmId=X — fields for a specific farm
    @GetMapping("/fields")
    public ResponseEntity<?> getAll(@RequestParam(required = false) Long farmId) {
        Long tenantId = authUtil.getCurrentTenantId();
        if (farmId != null) {
            return ResponseEntity.ok(ApiResponse.success(
                    fieldRepository.findByFarmIdAndTenantId(farmId, tenantId)
            ));
        }
        return ResponseEntity.ok(ApiResponse.success(
                fieldRepository.findByFarm_TenantId(tenantId)
        ));
    }

    @PostMapping("/fields")
    public ResponseEntity<?> create(@RequestBody Field req) {
        // Ensure farm is loaded properly
        if (req.getFarm() != null && req.getFarm().getId() != null) {
            Farm farm = farmRepository.findById(req.getFarm().getId()).orElseThrow();
            req.setFarm(farm);
            req.setTenant(farm.getTenant());
        }
        return ResponseEntity.ok(ApiResponse.success(fieldRepository.save(req), "Field added"));
    }

    @DeleteMapping("/fields/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        fieldRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Field deleted"));
    }
}