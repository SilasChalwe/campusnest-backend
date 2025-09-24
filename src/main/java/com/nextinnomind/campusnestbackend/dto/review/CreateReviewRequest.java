package com.nextinnomind.campusnestbackend.dto.review;

import lombok.Data;

@Data
public class CreateReviewRequest {
    private Long propertyId;
    private Integer rating; // 1-5
    private String comment;
}
