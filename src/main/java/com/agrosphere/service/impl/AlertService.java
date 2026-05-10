package com.agrosphere.service.impl;

import com.agrosphere.entity.Order;
import com.agrosphere.entity.Product;
import com.agrosphere.entity.User;
import com.agrosphere.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    // ── FARMER ALERTS ──────────────────────────────────────────────────────

    @Async
    public void notifyFarmerNewOrder(User farmer, User consumer, Order order) {
        String items = order.getItems() != null
            ? order.getItems().stream()
                .map(i -> i.getProduct().getName() + " x" + i.getQuantity())
                .collect(Collectors.joining(", "))
            : "Items";
        String amount = order.getTotalAmount().toString();

        // Email
        if (farmer.getEmail() != null) {
            emailService.sendHtml(
                farmer.getEmail(),
                "🛒 New Order " + order.getOrderNumber() + " | AgroSphere",
                emailService.newOrderTemplate(farmer.getName(), order.getOrderNumber(),
                    consumer.getName(), amount, items)
            );
        }
        // WhatsApp
        if (farmer.getPhoneNumber() != null) {
            whatsAppService.send(farmer.getPhoneNumber(),
                whatsAppService.newOrderMsg(farmer.getName(), order.getOrderNumber(),
                    consumer.getName(), amount)
            );
        }
    }

    @Async
    public void notifyFarmerLowStock(User farmer, Product product) {
        int qty = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        if (qty > 10) return;

        if (farmer.getEmail() != null) {
            emailService.sendHtml(
                farmer.getEmail(),
                "⚠️ Low Stock: " + product.getName() + " | AgroSphere",
                emailService.lowStockTemplate(farmer.getName(), product.getName(), qty, product.getUnit())
            );
        }
        if (farmer.getPhoneNumber() != null) {
            whatsAppService.send(farmer.getPhoneNumber(),
                whatsAppService.lowStockMsg(farmer.getName(), product.getName(), qty, product.getUnit())
            );
        }
    }

    @Async
    public void notifyFarmerPayment(User farmer, Order order) {
        if (farmer.getEmail() != null) {
            emailService.sendHtml(
                farmer.getEmail(),
                "💰 Payment Received: " + order.getOrderNumber() + " | AgroSphere",
                emailService.paymentTemplate(farmer.getName(), order.getOrderNumber(),
                    order.getTotalAmount().toString(), true)
            );
        }
        if (farmer.getPhoneNumber() != null) {
            whatsAppService.send(farmer.getPhoneNumber(),
                whatsAppService.paymentMsg(farmer.getName(), order.getOrderNumber(),
                    order.getTotalAmount().toString(), true)
            );
        }
    }

    // ── CONSUMER ALERTS ────────────────────────────────────────────────────

    @Async
    public void notifyConsumerStatusChange(User consumer, Order order, OrderStatus newStatus) {
        String emoji, color, msg;
        switch (newStatus) {
            case ACCEPTED  -> { emoji = "✅"; color = "#22c55e"; msg = "Your order has been confirmed by the farmer!"; }
            case SHIPPED   -> { emoji = "🚚"; color = "#3b82f6"; msg = "Your order is on the way!"; }
            case DELIVERED -> { emoji = "🎉"; color = "#6366f1"; msg = "Your order has been delivered. Enjoy your fresh produce!"; }
            case CANCELLED -> { emoji = "❌"; color = "#ef4444"; msg = "Your order has been cancelled."; }
            default        -> { emoji = "📦"; color = "#f59e0b"; msg = "Your order status has been updated."; }
        }

        // Email
        if (consumer.getEmail() != null) {
            emailService.sendHtml(
                consumer.getEmail(),
                emoji + " Order " + newStatus.name() + ": " + order.getOrderNumber() + " | AgroSphere",
                emailService.orderStatusTemplate(consumer.getName(), order.getOrderNumber(),
                    newStatus.name(), msg, color, emoji)
            );
        }

        // WhatsApp
        if (consumer.getPhoneNumber() != null) {
            String waMsg = newStatus == OrderStatus.ACCEPTED
                ? whatsAppService.orderConfirmedMsg(consumer.getName(), order.getOrderNumber(),
                    order.getFarmerTenant() != null ? order.getFarmerTenant().getName() : "Farmer")
                : whatsAppService.orderStatusMsg(consumer.getName(), order.getOrderNumber(),
                    newStatus.name(), emoji);
            whatsAppService.send(consumer.getPhoneNumber(), waMsg);
        }

        // Extra payment alert on delivery
        if (newStatus == OrderStatus.DELIVERED) {
            if (consumer.getEmail() != null) {
                emailService.sendHtml(
                    consumer.getEmail(),
                    "✅ Payment Successful: " + order.getOrderNumber() + " | AgroSphere",
                    emailService.paymentTemplate(consumer.getName(), order.getOrderNumber(),
                        order.getTotalAmount().toString(), false)
                );
            }
            if (consumer.getPhoneNumber() != null) {
                whatsAppService.send(consumer.getPhoneNumber(),
                    whatsAppService.paymentMsg(consumer.getName(), order.getOrderNumber(),
                        order.getTotalAmount().toString(), false)
                );
            }
        }
    }
}
