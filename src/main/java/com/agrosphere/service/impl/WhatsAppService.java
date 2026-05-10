package com.agrosphere.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-from:whatsapp:+14155238886}")
    private String fromNumber;

    @Value("${twilio.enabled:true}")
    private boolean enabled;

    @Async
    public void send(String toPhone, String message) {
        if (!enabled || toPhone == null || toPhone.isBlank()) return;

        // Format phone number → whatsapp:+91XXXXXXXXXX
        String phone = toPhone.replaceAll("[^0-9]", ""); // digits only
        if (phone.startsWith("0")) phone = phone.substring(1); // remove leading 0
        while (phone.startsWith("91") && phone.length() > 10) phone = phone.substring(2); // strip 91 prefix
        if (phone.length() == 10) phone = "91" + phone; // add country code
        String toWhatsApp = "whatsapp:+" + phone;

        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

            String body = "To=" + URLEncoder.encode(toWhatsApp, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(fromNumber, StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            String auth = Base64.getEncoder().encodeToString((accountSid + ":" + authToken).getBytes());

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> res = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() == 201) {
                log.info("WhatsApp sent to {}", toWhatsApp);
            } else {
                log.warn("WhatsApp failed to {}: {} - {}", toWhatsApp, res.statusCode(), res.body());
            }
        } catch (Exception e) {
            log.error("WhatsApp error to {}: {}", toWhatsApp, e.getMessage());
        }
    }

    // ── WHATSAPP MESSAGE TEMPLATES ─────────────────────────────────────────

    public String newOrderMsg(String farmerName, String orderNum, String consumerName, String amount) {
        return "🌱 *AgroSphere* - మన తోట మన జీవితం\n\n" +
               "🛒 *New Order Received!*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "👤 Customer: *" + consumerName + "*\n" +
               "📦 Order ID: *" + orderNum + "*\n" +
               "💰 Amount: *₹" + amount + "*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "Please login to AgroSphere and confirm the order. ✅";
    }

    public String orderStatusMsg(String consumerName, String orderNum, String status, String emoji) {
        return "🌱 *AgroSphere* - మన తోట మన జీవితం\n\n" +
               emoji + " *Order " + status + "!*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "Hello *" + consumerName + "*,\n" +
               "📦 Order: *" + orderNum + "*\n" +
               "📋 Status: *" + status + "*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               statusNote(status);
    }

    public String lowStockMsg(String farmerName, String productName, int qty, String unit) {
        return "🌱 *AgroSphere* - మన తోట మన జీవితం\n\n" +
               "⚠️ *Low Stock Alert!*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "Hello *" + farmerName + "*,\n" +
               "🌿 Product: *" + productName + "*\n" +
               "📉 Remaining: *" + qty + " " + unit + "*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "Please restock soon to avoid missing orders! 🙏";
    }

    public String paymentMsg(String name, String orderNum, String amount, boolean isFarmer) {
        return "🌱 *AgroSphere* - మన తోట మన జీవితం\n\n" +
               "💰 *Payment " + (isFarmer ? "Received" : "Successful") + "!*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "Hello *" + name + "*,\n" +
               "📦 Order: *" + orderNum + "*\n" +
               "💳 Amount: *₹" + amount + "*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               (isFarmer ? "Amount will be credited to your account. 🎉" : "Thank you for shopping with AgroSphere! 🛒");
    }

    public String orderConfirmedMsg(String consumerName, String orderNum, String farmerName) {
        return "🌱 *AgroSphere* - మన తోట మన జీవితం\n\n" +
               "✅ *Order Confirmed!*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "Hello *" + consumerName + "*,\n" +
               "📦 Order: *" + orderNum + "*\n" +
               "🧑‍🌾 Farmer: *" + farmerName + "*\n" +
               "━━━━━━━━━━━━━━━━\n" +
               "Your order is confirmed! We will notify you when dispatched. 🚚";
    }

    private String statusNote(String status) {
        return switch (status.toUpperCase()) {
            case "ACCEPTED"  -> "Your order is confirmed by the farmer! 🙏";
            case "SHIPPED"   -> "Your order is on the way! Be ready to receive it. 🚚";
            case "DELIVERED" -> "Enjoy your fresh farm produce! 🎉 Please rate your experience.";
            case "CANCELLED" -> "Sorry, your order was cancelled. Contact support for help. 😔";
            default          -> "Track your order in the AgroSphere app.";
        };
    }
}
