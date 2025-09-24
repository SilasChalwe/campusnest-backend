package com.nextinnomind.campusnestbackend.controller;

import com.nextinnomind.campusnestbackend.dto.Report.ReportResponse;
import com.nextinnomind.campusnestbackend.dto.auth.*;
import com.nextinnomind.campusnestbackend.dto.common.ApiResponse;
import com.nextinnomind.campusnestbackend.dto.property.PropertyResponse;
import com.nextinnomind.campusnestbackend.dto.user.UserResponse;
import com.nextinnomind.campusnestbackend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin management APIs")
public class AdminController {

    private final UserService userService;
    private final PropertyService propertyService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PutMapping("/users/{id}/deactivate")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activate user")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        // Implementation in service layer
        //TODO
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
    }

    @PutMapping("/properties/{id}/verify")
    @Operation(summary = "Verify property")
    public ResponseEntity<ApiResponse<PropertyResponse>> verifyProperty(@PathVariable Long id) {
        PropertyResponse response = propertyService.verifyProperty(id);
        return ResponseEntity.ok(ApiResponse.success("Property verified successfully", response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        // Add statistics logic
        //TODO
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get reports and flagged content")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReports(
            @PageableDefault(size = 20) Pageable pageable) {
        // Implementation needed
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved successfully", null));
    }
}