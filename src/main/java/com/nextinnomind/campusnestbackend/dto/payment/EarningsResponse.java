package com.nextinnomind.campusnestbackend.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EarningsResponse {
    private BigDecimal totalEarnings;
    private String period; // e.g., "month", "week", "year"
    private Long paymentCount;
}
