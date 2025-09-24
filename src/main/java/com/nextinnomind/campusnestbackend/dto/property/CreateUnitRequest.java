
package com.nextinnomind.campusnestbackend.dto.property;

import com.nextinnomind.campusnestbackend.enums.UnitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateUnitRequest {
    @NotBlank(message = "Unit name is required")
    private String name;

    @NotNull(message = "Unit type is required")
    private UnitType type;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Bedroom count is required")
    @Min(value = 0, message = "Bedroom count cannot be negative")
    private Integer bedroomCount;

    @NotNull(message = "Bathroom count is required")
    @Min(value = 1, message = "Bathroom count must be at least 1")
    private Integer bathroomCount;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.01", message = "Monthly rent must be greater than 0")
    private BigDecimal monthlyRent;

    @DecimalMin(value = "0.00", message = "Security deposit cannot be negative")
    private BigDecimal securityDeposit;

    private String description;

    private List<String> amenities;
}