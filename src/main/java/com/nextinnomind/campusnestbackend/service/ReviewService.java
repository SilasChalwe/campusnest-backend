
package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.dto.review.*;
import com.nextinnomind.campusnestbackend.entity.Property;
import com.nextinnomind.campusnestbackend.entity.Review;
import com.nextinnomind.campusnestbackend.entity.User;
import com.nextinnomind.campusnestbackend.exception.BadRequestException;
import com.nextinnomind.campusnestbackend.exception.ResourceNotFoundException;
import com.nextinnomind.campusnestbackend.repository.PropertyRepository;
import com.nextinnomind.campusnestbackend.repository.ReviewRepository;
import com.nextinnomind.campusnestbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ReviewResponse createReview(Long propertyId, CreateReviewRequest request, Long reviewerId) {
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user has already reviewed this property
        if (reviewRepository.findByPropertyIdAndReviewerId(request.getPropertyId(), reviewerId).isPresent()) {
            throw new BadRequestException("You have already reviewed this property");
        }

        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .property(property)
                .reviewer(reviewer)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        log.info("Created review {} for property {} by user {}",
                savedReview.getId(), property.getId(), reviewerId);

        return convertToResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPropertyReviews(Long propertyId, Pageable pageable) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found");
        }

        return reviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getUserReviews(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return reviewRepository.findByReviewerIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getPropertyReviewSummary(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found");
        }

        Double averageRating = reviewRepository.getAverageRatingByPropertyId(propertyId);
        Long totalReviews = reviewRepository.getReviewCountByPropertyId(propertyId);

        return ReviewSummaryResponse.builder()
                .propertyId(propertyId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews.intValue())
                .build();
    }

    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request, Long reviewerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new BadRequestException("You can only update your own reviews");
        }

        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new BadRequestException("Rating must be between 1 and 5");
            }
            review.setRating(request.getRating());
        }

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);

        log.info("Updated review {} by user {}", reviewId, reviewerId);

        return convertToResponse(updatedReview);
    }

    public void deleteReview(Long reviewId, Long reviewerId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getReviewer().getId().equals(reviewerId)) {
            throw new BadRequestException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);

        log.info("Deleted review {} by user {}", reviewId, reviewerId);
    }

    private ReviewResponse convertToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .propertyId(review.getProperty().getId())
                .propertyTitle(review.getProperty().getTitle())
                .reviewer(ReviewResponse.ReviewerInfo.builder()
                        .id(review.getReviewer().getId())
                        .fullName(review.getReviewer().getFullName())
                        .profilePictureUrl(review.getReviewer().getProfilePictureUrl())
                        .build())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}