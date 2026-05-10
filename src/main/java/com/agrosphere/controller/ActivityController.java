package com.agrosphere.controller;
import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.entity.CropActivity;
import com.agrosphere.enums.ActivityStatus;
import com.agrosphere.repository.CropActivityRepository;
import com.agrosphere.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/farmer") @RequiredArgsConstructor
public class ActivityController {
    private final CropActivityRepository activityRepository;
    private final AuthUtil authUtil;

    @GetMapping("/activities") public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.success(activityRepository.findByCrop_TenantId(authUtil.getCurrentTenantId())));
    }
    @GetMapping("/activities/pending") public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(ApiResponse.success(activityRepository.findByCrop_TenantIdAndStatus(authUtil.getCurrentTenantId(), ActivityStatus.PENDING)));
    }
    @PatchMapping("/activities/{id}/status") public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam ActivityStatus status) {
        CropActivity a = activityRepository.findById(id).orElseThrow();
        a.setStatus(status); activityRepository.save(a);
        return ResponseEntity.ok(ApiResponse.success(a, "Updated"));
    }
}
