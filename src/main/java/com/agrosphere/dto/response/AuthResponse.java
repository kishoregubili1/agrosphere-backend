package com.agrosphere.dto.response;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;
    private Long tenantId;
    private String profileImage;
}
