package com.nextinnomind.campusnestbackend.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
