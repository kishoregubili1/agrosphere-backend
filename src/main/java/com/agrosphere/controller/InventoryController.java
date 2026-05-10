package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.InventoryItem;
import com.agrosphere.entity.Tenant;
import com.agrosphere.repository.InventoryItemRepository;
import com.agrosphere.repository.TenantRepository;
import com.agrosphere.util.AuthUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/farmer/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryItemRepository repo;
    private final TenantRepository tenantRepo;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<?> getAll() {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        return ResponseEntity.ok(ApiResponse.success(repo.findByTenantOrderByCreatedAtDesc(t)));
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Req req) {
        Tenant t = tenantRepo.findById(authUtil.getCurrentTenantId()).orElseThrow();
        InventoryItem item = InventoryItem.builder()
            .tenant(t).name(req.name).category(req.category)
            .quantity(req.quantity).unit(req.unit == null ? "kg" : req.unit)
            .minStock(req.minStock == null ? 0.0 : req.minStock)
            .costPerUnit(req.costPerUnit == null ? 0.0 : req.costPerUnit)
            .supplier(req.supplier).notes(req.notes)
            .purchaseDate(req.purchaseDate != null ? LocalDate.parse(req.purchaseDate) : null)
            .expiryDate(req.expiryDate != null && !req.expiryDate.isBlank() ? LocalDate.parse(req.expiryDate) : null)
            .build();
        return ResponseEntity.ok(ApiResponse.success(repo.save(item), "Added"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Req req) {
        InventoryItem item = repo.findById(id).orElseThrow();
        if (!item.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        item.setName(req.name); item.setCategory(req.category); item.setQuantity(req.quantity);
        if (req.unit != null) item.setUnit(req.unit);
        if (req.minStock != null) item.setMinStock(req.minStock);
        if (req.costPerUnit != null) item.setCostPerUnit(req.costPerUnit);
        item.setSupplier(req.supplier); item.setNotes(req.notes);
        if (req.purchaseDate != null) item.setPurchaseDate(LocalDate.parse(req.purchaseDate));
        item.setExpiryDate(req.expiryDate != null && !req.expiryDate.isBlank() ? LocalDate.parse(req.expiryDate) : null);
        return ResponseEntity.ok(ApiResponse.success(repo.save(item), "Updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        InventoryItem item = repo.findById(id).orElseThrow();
        if (!item.getTenant().getId().equals(authUtil.getCurrentTenantId())) return ResponseEntity.status(403).build();
        repo.delete(item);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @Data static class Req {
        String name, category, unit, supplier, purchaseDate, expiryDate, notes;
        Double quantity, minStock, costPerUnit;
    }
}
