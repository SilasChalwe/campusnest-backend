package com.nextinnomind.campusnestbackend.dto.payment;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EarningsPeriod {
    private Long landlordId;
    private LocalDate startDate;
    private LocalDate endDate;
}
