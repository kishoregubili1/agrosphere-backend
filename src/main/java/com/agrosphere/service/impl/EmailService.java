package com.agrosphere.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:AgroSphere <noreply@agrosphere.com>}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean enabled;

    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        if (!enabled || to == null || to.isBlank()) return;
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Email failed to {}: {}", to, e.getMessage());
        }
    }

    // ── EMAIL TEMPLATES ────────────────────────────────────────────────────

    public String newOrderTemplate(String farmerName, String orderNum, String consumerName, String amount, String items) {
        return baseTemplate("🛒 New Order Received!", "#22c55e",
            "<h2 style='color:#22c55e;margin:0 0 8px'>New Order: " + orderNum + "</h2>" +
            "<p style='color:#555;font-size:15px;margin:0 0 16px'>Hello <b>" + farmerName + "</b>, you have a new order!</p>" +
            infoRow("👤 Customer", consumerName) +
            infoRow("📦 Order ID", orderNum) +
            infoRow("🛍️ Items", items) +
            infoRow("💰 Total Amount", "₹" + amount) +
            "<div style='text-align:center;margin-top:24px'>" +
            actionBtn("View Order", "#22c55e") +
            "</div><p style='color:#888;font-size:12px;margin-top:16px;text-align:center'>Please confirm the order within 30 minutes.</p>"
        );
    }

    public String orderStatusTemplate(String consumerName, String orderNum, String status, String statusMsg, String color, String emoji) {
        return baseTemplate(emoji + " Order " + status, color,
            "<h2 style='color:" + color + ";margin:0 0 8px'>" + emoji + " " + statusMsg + "</h2>" +
            "<p style='color:#555;font-size:15px;margin:0 0 16px'>Hello <b>" + consumerName + "</b>,</p>" +
            infoRow("📦 Order ID", orderNum) +
            infoRow("📋 Status", status) +
            "<p style='color:#555;font-size:14px;margin-top:16px'>" + statusMsg + "</p>" +
            "<div style='text-align:center;margin-top:24px'>" +
            actionBtn("Track Order", color) + "</div>"
        );
    }

    public String lowStockTemplate(String farmerName, String productName, int qty, String unit) {
        return baseTemplate("⚠️ Low Stock Alert", "#f59e0b",
            "<h2 style='color:#f59e0b;margin:0 0 8px'>⚠️ Low Stock Warning</h2>" +
            "<p style='color:#555;font-size:15px;margin:0 0 16px'>Hello <b>" + farmerName + "</b>,</p>" +
            infoRow("🌿 Product", productName) +
            infoRow("📉 Remaining Stock", qty + " " + unit) +
            "<p style='color:#f59e0b;font-size:14px;margin-top:16px;font-weight:bold'>Please restock soon to avoid missing orders!</p>" +
            "<div style='text-align:center;margin-top:24px'>" +
            actionBtn("Update Stock", "#f59e0b") + "</div>"
        );
    }

    public String paymentTemplate(String name, String orderNum, String amount, boolean isFarmer) {
        String title = isFarmer ? "💰 Payment Received!" : "✅ Payment Successful!";
        String msg = isFarmer
            ? "Payment of ₹" + amount + " received for order " + orderNum
            : "Your payment of ₹" + amount + " for order " + orderNum + " was successful!";
        return baseTemplate(title, "#6366f1",
            "<h2 style='color:#6366f1;margin:0 0 8px'>" + title + "</h2>" +
            "<p style='color:#555;font-size:15px;margin:0 0 16px'>Hello <b>" + name + "</b>,</p>" +
            infoRow("📦 Order ID", orderNum) +
            infoRow("💳 Amount", "₹" + amount) +
            "<p style='color:#555;font-size:14px;margin-top:16px'>" + msg + "</p>"
        );
    }

    // ── HELPERS ────────────────────────────────────────────────────────────

    private String infoRow(String label, String value) {
        return "<div style='display:flex;justify-content:space-between;padding:10px 0;border-bottom:1px solid #f0f0f0'>" +
               "<span style='color:#888;font-size:13px'>" + label + "</span>" +
               "<span style='color:#222;font-size:13px;font-weight:600'>" + value + "</span></div>";
    }

    private String actionBtn(String text, String color) {
        return "<a href='#' style='display:inline-block;background:" + color + ";color:#fff;text-decoration:none;" +
               "padding:12px 32px;border-radius:10px;font-weight:700;font-size:14px'>" + text + "</a>";
    }

    private String baseTemplate(String title, String accentColor, String body) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f5f5f5;font-family:sans-serif'>" +
               "<div style='max-width:520px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08)'>" +
               "<div style='background:linear-gradient(135deg," + accentColor + ",#1a1a2e);padding:28px 32px;text-align:center'>" +
               "<h1 style='color:#fff;margin:0;font-size:22px;font-weight:800'>🌱 AgroSphere</h1>" +
               "<p style='color:rgba(255,255,255,.7);margin:6px 0 0;font-size:13px'>మన తోట మన జీవితం</p></div>" +
               "<div style='padding:28px 32px'>" + body + "</div>" +
               "<div style='background:#f9f9f9;padding:16px 32px;text-align:center'>" +
               "<p style='color:#aaa;font-size:11px;margin:0'>AgroSphere · Direct from Farm to You</p></div>" +
               "</div></body></html>";
    }
}
