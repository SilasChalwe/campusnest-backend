package com.nextinnomind.campusnestbackend.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String address;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private String profilePictureUrl;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
