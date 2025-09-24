
package com.nextinnomind.campusnestbackend.dto.property;

import com.nextinnomind.campusnestbackend.enums.UnitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitResponse {
    private Long id;
    private String name;
    private UnitType type;
    private Integer capacity;
    private Integer bedroomCount;
    private Integer bathroomCount;
    private BigDecimal monthlyRent;
    private BigDecimal securityDeposit;
    private Boolean available;
    private String description;
    private List<String> amenities;
}