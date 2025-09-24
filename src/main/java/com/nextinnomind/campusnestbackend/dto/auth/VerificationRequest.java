
package com.nextinnomind.campusnestbackend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerificationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Verification code is required")
    private String code;
}