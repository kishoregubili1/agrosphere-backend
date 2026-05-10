package com.agrosphere.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AIDoctorController {

    @Value("${anthropic.api-key:}")
    private String apiKey;

    @PostMapping("/analyze-crop")
    public ResponseEntity<String> analyzeCrop(@RequestBody Map<String, Object> body) {
        try {
            String requestBody = buildRequest(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anthropic.com/v1/messages"))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return ResponseEntity.status(response.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.body());

        } catch (Exception e) {
            log.error("AI Doctor error: {}", e.getMessage());
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String buildRequest(Map<String, Object> body) {
        String base64 = (String) body.get("base64");
        String mediaType = (String) body.getOrDefault("mediaType", "image/jpeg");
        String cropName = (String) body.getOrDefault("cropName", "");

        String cropHint = cropName != null && !cropName.isBlank() ? " (crop: " + cropName + ")" : "";
        String prompt = "You are an expert agricultural disease detection AI for Indian farmers. Analyze this crop image" + cropHint + ".\\n\\nRespond ONLY with a JSON object, no markdown, no explanation:\\n{\\n  \\\"diseaseName\\\": \\\"disease name in English\\\",\\n  \\\"diseaseNameTelugu\\\": \\\"disease name in Telugu\\\",\\n  \\\"confidence\\\": 85,\\n  \\\"severity\\\": \\\"LOW|MEDIUM|HIGH|CRITICAL\\\",\\n  \\\"isHealthy\\\": false,\\n  \\\"description\\\": \\\"Brief description in English\\\",\\n  \\\"descriptionTelugu\\\": \\\"Brief description in Telugu\\\",\\n  \\\"symptoms\\\": [\\\"symptom 1\\\", \\\"symptom 2\\\", \\\"symptom 3\\\"],\\n  \\\"symptomsTelugu\\\": [\\\"లక్షణం 1\\\", \\\"లక్షణం 2\\\", \\\"లక్షణం 3\\\"],\\n  \\\"organicTreatments\\\": [{\\\"step\\\": \\\"Step 1\\\", \\\"stepTelugu\\\": \\\"దశ 1\\\"}],\\n  \\\"chemicalTreatments\\\": [{\\\"step\\\": \\\"Step 1\\\", \\\"stepTelugu\\\": \\\"దశ 1\\\"}],\\n  \\\"preventionTips\\\": [\\\"tip 1\\\", \\\"tip 2\\\"],\\n  \\\"preventionTipsTelugu\\\": [\\\"చిట్కా 1\\\", \\\"చిట్కా 2\\\"]\\n}";

        return "{"
                + "\"model\":\"claude-sonnet-4-20250514\","
                + "\"max_tokens\":1500,"
                + "\"messages\":[{"
                + "\"role\":\"user\","
                + "\"content\":["
                + "{\"type\":\"image\",\"source\":{\"type\":\"base64\",\"media_type\":\"" + mediaType + "\",\"data\":\"" + base64 + "\"}},"
                + "{\"type\":\"text\",\"text\":\"" + prompt + "\"}"
                + "]}]}";
    }
}