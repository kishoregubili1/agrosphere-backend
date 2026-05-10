package com.agrosphere.controller;

import com.agrosphere.dto.response.ApiResponse;
import com.agrosphere.service.impl.NotificationService;
import com.agrosphere.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(authUtil.getCurrentUserId())));
    }
    @GetMapping("/count")
    public ResponseEntity<?> count() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(authUtil.getCurrentUserId())));
    }
    @PatchMapping("/read-all")
    public ResponseEntity<?> readAll() {
        notificationService.markAllRead(authUtil.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null,"All read"));
    }
}
