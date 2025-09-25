package com.nextinnomind.campusnestbackend.controller;

import com.nextinnomind.campusnestbackend.dto.booking.*;
import com.nextinnomind.campusnestbackend.dto.common.ApiResponse;
import com.nextinnomind.campusnestbackend.security.CurrentUser;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import com.nextinnomind.campusnestbackend.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management APIs")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create booking request")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking request created successfully", response));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student's bookings")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(@CurrentUser UserPrincipal userPrincipal, @PageableDefault(size = 20) Pageable pageable) {
        Page<BookingResponse> bookings = bookingService.getUserBookings(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Get property bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getPropertyBookings(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long propertyId) {
        List<BookingResponse> bookings = bookingService.getPropertyBookings(propertyId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Property bookings retrieved successfully", bookings));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get booking details")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Approve booking request")
    public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestBody(required = false) BookingResponseRequest request) {
        BookingResponse response = bookingService.approveBooking(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Booking approved successfully", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Reject booking request")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody BookingResponseRequest request) {
        BookingResponse response = bookingService.rejectBooking(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Booking rejected successfully", response));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Cancel booking request")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        BookingResponse response = bookingService.cancelBooking(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }
}