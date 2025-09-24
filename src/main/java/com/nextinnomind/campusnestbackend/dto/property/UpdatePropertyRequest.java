package com.nextinnomind.campusnestbackend.dto.property;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdatePropertyRequest {
    private String title;
    private String description;
    private String address;
    private BigDecimal basePrice;
    private List<String> amenities;
    private LocalDate availableFrom;
}
