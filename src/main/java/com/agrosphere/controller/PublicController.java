package com.agrosphere.controller;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.repository.CropTypeRepository;
import com.agrosphere.repository.DiseaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/public") @RequiredArgsConstructor
public class PublicController {
    private final CropTypeRepository cropTypeRepository;
    private final DiseaseRepository diseaseRepository;
    @GetMapping("/crop-types") public ResponseEntity<?> getCropTypes() {
        return ResponseEntity.ok(ApiResponse.success(cropTypeRepository.findAll()));
    }
    @GetMapping("/diseases") public ResponseEntity<?> getDiseases() {
        return ResponseEntity.ok(ApiResponse.success(diseaseRepository.findAll()));
    }
}
