
package com.nextinnomind.campusnestbackend.repository;

import com.nextinnomind.campusnestbackend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByPropertyIdOrderByCreatedAtDesc(Long propertyId, Pageable pageable);
    Page<Review> findByReviewerIdOrderByCreatedAtDesc(Long reviewerId, Pageable pageable);
    Optional<Review> findByPropertyIdAndReviewerId(Long propertyId, Long reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.property.id = :propertyId")
    Double getAverageRatingByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.property.id = :propertyId")
    Long getReviewCountByPropertyId(@Param("propertyId") Long propertyId);
}