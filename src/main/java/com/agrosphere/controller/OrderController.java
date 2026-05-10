package com.agrosphere.controller;

import com.agrosphere.dto.request.OrderRequest;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.enums.OrderStatus;
import com.agrosphere.service.impl.OrderService;
import com.agrosphere.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthUtil authUtil;

    // ─── CONSUMER ─────────────────────────────────
    @PostMapping("/consumer/orders")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.placeOrder(req, authUtil.getCurrentUserId()), "Order placed!"));
    }

    @GetMapping("/consumer/orders")
    public ResponseEntity<?> getMyOrdersAsConsumer() {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getMyOrdersAsConsumer(authUtil.getCurrentUserId())));
    }

    // ─── FARMER ───────────────────────────────────
    @GetMapping("/farmer/orders")
    public ResponseEntity<?> getIncomingOrders() {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getIncomingOrdersAsFarmer(authUtil.getCurrentTenantId())));
    }

    @PatchMapping("/farmer/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.updateStatus(id, status, authUtil.getCurrentTenantId()), "Order status updated"));
    }
}
