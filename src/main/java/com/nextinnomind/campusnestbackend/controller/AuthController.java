package com.nextinnomind.campusnestbackend.controller;

import com.nextinnomind.campusnestbackend.dto.auth.*;
import com.nextinnomind.campusnestbackend.dto.common.ApiResponse;
import com.nextinnomind.campusnestbackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful! Please verify your email and phone.", response));
        } catch (Exception e) {
            log.error("Registration failed", e);
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw e;
        }
    }

    @PostMapping("/verify/email")
    @Operation(summary = "Verify email address")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyEmail(@Valid @RequestBody VerificationRequest request) {
        try {
            authService.verifyEmail(request);

            // Get updated verification status
            Map<String, Object> status = authService.getVerificationStatus(request.getUserId());
            status.put("message", "Email verified successfully!");

            return ResponseEntity.ok(ApiResponse.success("Email verified successfully", status));
        } catch (Exception e) {
            log.error("Email verification failed for userId: {}", request.getUserId(), e);
            throw e;
        }
    }

    @PostMapping("/verify/phone")
    @Operation(summary = "Verify phone number")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPhone(@Valid @RequestBody VerificationRequest request) {
        try {
            authService.verifyPhone(request);

            // Get updated verification status
            Map<String, Object> status = authService.getVerificationStatus(request.getUserId());
            status.put("message", "Phone verified successfully!");

            return ResponseEntity.ok(ApiResponse.success("Phone verified successfully", status));
        } catch (Exception e) {
            log.error("Phone verification failed for userId: {}", request.getUserId(), e);
            throw e;
        }
    }

    @GetMapping("/verification-status/{userId}")
    @Operation(summary = "Get verification status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVerificationStatus(@PathVariable Long userId) {
        try {
            Map<String, Object> status = authService.getVerificationStatus(userId);
            return ResponseEntity.ok(ApiResponse.success("Verification status retrieved", status));
        } catch (Exception e) {
            log.error("Failed to get verification status for userId: {}", userId, e);
            throw e;
        }
    }

    @PostMapping("/resend/email/{userId}")
    @Operation(summary = "Resend email verification code")
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification(@PathVariable Long userId) {
        try {
            authService.resendEmailVerification(userId);
            return ResponseEntity.ok(ApiResponse.success("Email verification code resent successfully", null));
        } catch (Exception e) {
            log.error("Resend email verification failed for userId: {}", userId, e);
            throw e;
        }
    }

    @PostMapping("/resend/phone/{userId}")
    @Operation(summary = "Resend phone verification code")
    public ResponseEntity<ApiResponse<Void>> resendPhoneVerification(@PathVariable Long userId) {
        try {
            authService.resendPhoneVerification(userId);
            return ResponseEntity.ok(ApiResponse.success("Phone verification code resent successfully", null));
        } catch (Exception e) {
            log.error("Resend phone verification failed for userId: {}", userId, e);
            throw e;
        }
    }





    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        try {
            // Extract JWT token from Authorization header
            String jwtToken = token.replace("Bearer ", "");

            // TODO: Implement logout logic
            // - Add token to blacklist
            // - Revoke refresh tokens for this user
            // - Clear any server-side sessions

            log.info("User logout requested with token: {}", jwtToken.substring(0, 10) + "...");
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw e;
        }
    }
}