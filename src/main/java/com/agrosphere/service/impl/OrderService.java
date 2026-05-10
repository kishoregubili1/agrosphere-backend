package com.agrosphere.service.impl;

import com.agrosphere.dto.request.OrderRequest;
import com.agrosphere.entity.*;
import com.agrosphere.enums.OrderStatus;
import com.agrosphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final NotificationService notificationService;
    private final AlertService alertService;

    @Transactional
    public Order placeOrder(OrderRequest req, Long consumerId) {
        User consumer = userRepository.findById(consumerId).orElseThrow();
        if (req.getItems() == null || req.getItems().isEmpty())
            throw new RuntimeException("Order must have at least one item");

        Map<Long, List<OrderRequest.OrderItemRequest>> byTenant = new LinkedHashMap<>();
        for (var item : req.getItems()) {
            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            if (!p.getIsActive()) throw new RuntimeException("Product not available: " + p.getName());
            if (p.getStockQuantity() == null || p.getStockQuantity() < item.getQuantity())
                throw new RuntimeException("Insufficient stock for: " + p.getName());
            byTenant.computeIfAbsent(p.getTenant().getId(), k -> new ArrayList<>()).add(item);
        }

        Order firstOrder = null;
        for (var entry : byTenant.entrySet()) {
            Tenant farmer = tenantRepository.findById(entry.getKey()).orElseThrow();
            BigDecimal total = BigDecimal.ZERO;
            String orderNum = "AGR-" + System.currentTimeMillis();

            Order order = orderRepository.save(Order.builder()
                    .consumer(consumer).farmerTenant(farmer)
                    .orderNumber(orderNum).status(OrderStatus.PENDING)
                    .deliveryAddress(req.getDeliveryAddress())
                    .totalAmount(BigDecimal.ZERO).build());

            List<Product> updatedProducts = new ArrayList<>();
            for (var ir : entry.getValue()) {
                Product p = productRepository.findById(ir.getProductId()).orElseThrow();
                p.setStockQuantity((p.getStockQuantity() != null ? p.getStockQuantity() : 0) - ir.getQuantity());
                productRepository.save(p);
                updatedProducts.add(p);
                BigDecimal lineTotal = p.getPrice().multiply(BigDecimal.valueOf(ir.getQuantity()));
                total = total.add(lineTotal);
                orderItemRepository.save(OrderItem.builder()
                        .order(order).product(p)
                        .quantity(ir.getQuantity()).price(p.getPrice()).build());
            }
            order.setTotalAmount(total);
            orderRepository.save(order);

            // In-app notification to farmer
            User farmerUser = userRepository.findByTenantAndRole(farmer);
            if (farmerUser != null) {
                notificationService.send(farmerUser,
                    "🛒 New Order: " + orderNum,
                    consumer.getName() + " ordered ₹" + total + ". Confirm now!",
                    "ORDER_PLACED", order.getId().toString());

                // Email + SMS to farmer
                alertService.notifyFarmerNewOrder(farmerUser, consumer, order);

                // Low stock check for each product
                for (Product p : updatedProducts) {
                    alertService.notifyFarmerLowStock(farmerUser, p);
                }
            }
            if (firstOrder == null) firstOrder = order;
        }
        return firstOrder;
    }

    public List<Order> getMyOrdersAsConsumer(Long consumerId) {
        return orderRepository.findByConsumerIdOrderByCreatedAtDesc(consumerId);
    }

    public List<Order> getIncomingOrdersAsFarmer(Long tenantId) {
        return orderRepository.findByFarmerTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus, Long tenantId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getFarmerTenant().getId().equals(tenantId))
            throw new RuntimeException("Unauthorized");
        order.setStatus(newStatus);
        orderRepository.save(order);

        // In-app notification to consumer
        if (order.getConsumer() != null) {
            String emoji = switch (newStatus) {
                case ACCEPTED -> "✅"; case SHIPPED -> "🚚";
                case DELIVERED -> "🎉"; case CANCELLED -> "❌"; default -> "📦";
            };
            notificationService.send(order.getConsumer(),
                emoji + " Order " + newStatus.name(),
                "Your order " + order.getOrderNumber() + " is now " + newStatus.name(),
                "ORDER_STATUS", orderId.toString());

            // Email + SMS to consumer
            alertService.notifyConsumerStatusChange(order.getConsumer(), order, newStatus);
        }

        // Payment alert to farmer on delivery
        if (newStatus == OrderStatus.DELIVERED) {
            User farmerUser = userRepository.findByTenantAndRole(order.getFarmerTenant());
            if (farmerUser != null) {
                alertService.notifyFarmerPayment(farmerUser, order);
            }
        }

        return order;
    }
}
