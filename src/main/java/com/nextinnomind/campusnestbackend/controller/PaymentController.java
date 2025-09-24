
package com.nextinnomind.campusnestbackend.controller;
import com.nextinnomind.campusnestbackend.dto.common.ApiResponse;
import com.nextinnomind.campusnestbackend.dto.payment.*;
import com.nextinnomind.campusnestbackend.security.CurrentUser;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import com.nextinnomind.campusnestbackend.service.PaymentService;
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

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Create payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment details")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's payments")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getUserPayments(
            @CurrentUser UserPrincipal userPrincipal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PaymentResponse> payments = paymentService.getUserPayments(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Confirm payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestParam String transactionId) {
        PaymentResponse response = paymentService.confirmPayment(id, transactionId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Payment confirmed successfully", response));
    }

    @GetMapping("/earnings")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Get landlord earnings")
    public ResponseEntity<ApiResponse<EarningsResponse>> getEarnings(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        // Build a period object combining landlord id + dates
        EarningsPeriod period = new EarningsPeriod(userPrincipal.getId(), startDate, endDate
        );

        EarningsResponse response = paymentService.getLandlordEarnings(period.getLandlordId(), String.valueOf(period));

        return ResponseEntity.ok(
                ApiResponse.success("Earnings retrieved successfully", response)
        );
    }


    @PostMapping("/webhook")
    @Operation(summary = "Payment provider webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestBody String payload) {
        paymentService.handleWebhook(signature, payload);
        return ResponseEntity.ok().build();
    }
}