package com.nextinnomind.campusnestbackend.dto.property;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PropertyResponse {
    private Long id;
    private String title;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal basePrice;
    private String currency;
    private List<String> photos;
    private List<String> amenities;
    private LocalDate availableFrom;
    private String status;
    private Boolean featured;
    private Integer viewCount;
    private OwnerInfo owner;
    private List<UnitResponse> units;
    private ReviewSummary reviewSummary;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class OwnerInfo {
        private Long id;
        private String fullName;
        private String phone;
        private String profilePictureUrl;
    }

    @Data
    @Builder
    public static class ReviewSummary {
        private Double averageRating;
        private Integer totalReviews;
    }
}
