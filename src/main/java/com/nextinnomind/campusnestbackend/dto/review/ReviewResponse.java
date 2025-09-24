package com.nextinnomind.campusnestbackend.dto.review;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private ReviewerInfo reviewer;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class ReviewerInfo {
        private Long id;
        private String fullName;
        private String profilePictureUrl;
    }
}
