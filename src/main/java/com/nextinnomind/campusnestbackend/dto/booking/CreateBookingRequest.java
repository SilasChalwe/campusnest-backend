package com.nextinnomind.campusnestbackend.dto.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Unit ID is required")
    private Long unitId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private String studentMessage;
}