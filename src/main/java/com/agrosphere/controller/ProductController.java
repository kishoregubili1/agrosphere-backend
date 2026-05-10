package com.agrosphere.controller;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.Product;
import com.agrosphere.entity.Tenant;
import com.agrosphere.repository.ProductRepository;
import com.agrosphere.repository.TenantRepository;
import com.agrosphere.service.impl.FileStorageService;
import com.agrosphere.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController @RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;
    private final FileStorageService fileStorageService;
    private final AuthUtil authUtil;

    @GetMapping("/marketplace/products")
    public ResponseEntity<?> browse(@RequestParam(required=false) String category) {
        var products = category != null && !category.isBlank()
            ? productRepository.findByCategoryAndIsActiveTrue(category)
            : productRepository.findByIsActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/farmer/products")
    public ResponseEntity<?> myProducts() {
        return ResponseEntity.ok(ApiResponse.success(productRepository.findByTenantId(authUtil.getCurrentTenantId())));
    }

    @PostMapping("/farmer/products")
    public ResponseEntity<?> create(@RequestBody Product req) {
        Tenant t = tenantRepository.findById(authUtil.getCurrentTenantId()).orElseThrow();
        req.setTenant(t);
        if (req.getIsActive() == null) req.setIsActive(true);
        return ResponseEntity.ok(ApiResponse.success(productRepository.save(req), "Product listed"));
    }

    @PutMapping("/farmer/products/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product req) {
        Product p = productRepository.findById(id).orElseThrow();
        if (!p.getTenant().getId().equals(authUtil.getCurrentTenantId())) throw new RuntimeException("Unauthorized");
        p.setName(req.getName()); p.setDescription(req.getDescription()); p.setPrice(req.getPrice());
        p.setUnit(req.getUnit()); p.setStockQuantity(req.getStockQuantity()); p.setCategory(req.getCategory());
        if (req.getImageUrl() != null) p.setImageUrl(req.getImageUrl());
        return ResponseEntity.ok(ApiResponse.success(productRepository.save(p), "Updated"));
    }

    @DeleteMapping("/farmer/products/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }

    @PostMapping("/farmer/products/{id}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Product p = productRepository.findById(id).orElseThrow();
        String url = fileStorageService.store(file, "products");
        p.setImageUrl(url);
        productRepository.save(p);
        return ResponseEntity.ok(ApiResponse.success(Map.of("imageUrl", url)));
    }
}
