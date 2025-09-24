package com.nextinnomind.campusnestbackend.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private Long bookingId;
    private BigDecimal amount;
    private String currency; // e.g., "USD"
    private String paymentMethod; // e.g., "card"
    private String description;
}
