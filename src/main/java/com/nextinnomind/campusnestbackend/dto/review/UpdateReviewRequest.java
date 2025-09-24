package com.nextinnomind.campusnestbackend.dto.review;

import lombok.Data;

@Data
public class UpdateReviewRequest {
    private Integer rating; // optional
    private String comment; // optional
}
