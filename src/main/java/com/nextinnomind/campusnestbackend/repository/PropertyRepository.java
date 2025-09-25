package com.nextinnomind.campusnestbackend.repository;

import com.nextinnomind.campusnestbackend.entity.Property;
import com.nextinnomind.campusnestbackend.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);
    Page<Property> findByOwnerId(Long ownerId, Pageable pageable);
    Page<Property> findByFeaturedTrue(Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.status = 'ACTIVE' " + "AND (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " + "OR LOWER(p.address) LIKE LOWER(CONCAT('%', :query, '%'))) " + "AND (:address IS NULL OR LOWER(p.address) LIKE LOWER(CONCAT('%', :address, '%'))) " + "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " + "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)")
    Page<Property> searchProperties(@Param("query") String query, @Param("address") String address, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);


    @Query("SELECT p FROM Property p WHERE p.status = 'ACTIVE' " + "AND (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " + "cos(radians(p.longitude) - radians(:lng)) + sin(radians(:lat)) * " + "sin(radians(p.latitude)))) <= :radiusKm")
    Page<Property> findPropertiesWithinRadius(@Param("lat") Double latitude, @Param("lng") Double longitude, @Param("radiusKm") Double radiusKm, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Property p JOIN p.amenities a " + "WHERE p.status = 'ACTIVE' AND a IN :amenities")
    Page<Property> findByAmenitiesIn(@Param("amenities") List<String> amenities, Pageable pageable);
}
