package com.agrosphere.service.impl;

import com.agrosphere.dto.request.ProductRequest;
import com.agrosphere.entity.*;
import com.agrosphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CropRepository cropRepository;
    private final TenantRepository tenantRepository;

    // Public marketplace
    public List<Product> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    public List<Product> getByCategory(String category) {
        return productRepository.findByCategoryAndIsActiveTrue(category);
    }

    // Farmer's own products
    public List<Product> getMyProducts(Long tenantId) {
        return productRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Transactional
    public Product create(ProductRequest req, Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        Crop crop = req.getCropId() != null
                ? cropRepository.findByIdAndTenantId(req.getCropId(), tenantId).orElse(null)
                : null;

        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .unit(req.getUnit())
                .stockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0)
                .category(req.getCategory())
                .imageUrl(req.getImageUrl())
                .crop(crop)
                .tenant(tenant)
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest req, Long tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setUnit(req.getUnit());
        product.setStockQuantity(req.getStockQuantity());
        product.setCategory(req.getCategory());
        product.setImageUrl(req.getImageUrl());
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id, Long tenantId) {
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }
}
