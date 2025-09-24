package com.nextinnomind.campusnestbackend.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private String status; // e.g., "PENDING", "COMPLETED"
    private String paymentMethod;
    private String description;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private String clientSecret; // Only returned on payment creation for Stripe
}
