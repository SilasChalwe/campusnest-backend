package com.nextinnomind.campusnestbackend.dto.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewSummaryResponse {
    private Long propertyId;
    private Double averageRating;
    private Integer totalReviews;
}
