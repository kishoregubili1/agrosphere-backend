package com.agrosphere.dto.request;
import lombok.Data;
@Data
public class RegisterRequest {
    private String name, email, password, phoneNumber, district, state;
}
