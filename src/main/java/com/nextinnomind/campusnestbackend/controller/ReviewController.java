package com.nextinnomind.campusnestbackend.controller;

import com.nextinnomind.campusnestbackend.dto.common.ApiResponse;
import com.nextinnomind.campusnestbackend.dto.review.*;
import com.nextinnomind.campusnestbackend.security.CurrentUser;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import com.nextinnomind.campusnestbackend.service.ReviewService;
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

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/properties/{propertyId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create property review")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long propertyId,
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(propertyId, request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", response));
    }

    @GetMapping("/properties/{propertyId}")
    @Operation(summary = "Get property reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPropertyReviews(
            @PathVariable Long propertyId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewResponse> reviews = reviewService.getPropertyReviews(propertyId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @GetMapping("/properties/{propertyId}/summary")
    @Operation(summary = "Get property review summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getReviewSummary(@PathVariable Long propertyId) {
        ReviewSummaryResponse summary = reviewService.getPropertyReviewSummary(propertyId);
        return ResponseEntity.ok(ApiResponse.success("Review summary retrieved successfully", summary));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Update review")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(id, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "Delete review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        reviewService.deleteReview(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}