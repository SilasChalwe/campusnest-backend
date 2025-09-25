package com.nextinnomind.campusnestbackend.service;

import com.nextinnomind.campusnestbackend.dto.property.*;
import com.nextinnomind.campusnestbackend.entity.Property;
import com.nextinnomind.campusnestbackend.entity.Unit;
import com.nextinnomind.campusnestbackend.entity.User;
import com.nextinnomind.campusnestbackend.enums.PropertyStatus;
import com.nextinnomind.campusnestbackend.exception.BadRequestException;
import com.nextinnomind.campusnestbackend.exception.ResourceNotFoundException;
import com.nextinnomind.campusnestbackend.repository.PropertyRepository;
import com.nextinnomind.campusnestbackend.repository.ReviewRepository;
import com.nextinnomind.campusnestbackend.repository.UnitRepository;
import com.nextinnomind.campusnestbackend.repository.UserRepository;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorageService fileStorageService;

    public Page<PropertyResponse> searchProperties(
            String query,
            String address,
            Double lat,
            Double lng,
            Double radiusKm,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            List<String> amenities,
            Pageable pageable) {

        Page<Property> properties;

        // If coordinates are provided, search by radius
        if (lat != null && lng != null) {
            properties = propertyRepository.findPropertiesWithinRadius(lat, lng, radiusKm, pageable);
        }
        // Search by keywords, address, and price range
        else if ((query != null && !query.isEmpty()) ||
                (address != null && !address.isEmpty()) ||
                minPrice != null || maxPrice != null) {
            properties = propertyRepository.searchProperties(query, address, minPrice, maxPrice, pageable);
        }
        // Search by amenities
        else if (amenities != null && !amenities.isEmpty()) {
            properties = propertyRepository.findByAmenitiesIn(amenities, pageable);
        }
        // Default: return all active properties
        else {
            properties = propertyRepository.findByStatus(PropertyStatus.ACTIVE, pageable);
        }

        return properties.map(this::convertToResponse);
    }




    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        // Increment view count
        property.setViewCount(property.getViewCount() == null ? 1 : property.getViewCount() + 1);
        propertyRepository.save(property);


        return convertToResponse(property);
    }

    public PropertyResponse createProperty(CreatePropertyRequest request, List<MultipartFile> photos, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .basePrice(request.getBasePrice())
                .currency(request.getCurrency())
                .amenities(request.getAmenities())
                .availableFrom(request.getAvailableFrom())
                .owner(owner)
                .status(PropertyStatus.PENDING_APPROVAL)
                .build();

        // Handle photo uploads
        if (photos != null && !photos.isEmpty()) {
            List<String> photoUrls = photos.stream()
                    .map(photo -> fileStorageService.storeFile(photo, "properties"))
                    .collect(Collectors.toList());
            property.setPhotos(photoUrls);
        }

        Property savedProperty = propertyRepository.save(property);
        log.info("Created new property: {} by user: {}", savedProperty.getId(), ownerId);

        return convertToResponse(savedProperty);
    }

    public PropertyResponse updateProperty(Long id, UpdatePropertyRequest request, UserPrincipal userPrincipal) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        // Check ownership or admin role
        if (!property.getOwner().getId().equals(userPrincipal.getId()) &&
                userPrincipal.getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BadRequestException("You can only update your own properties");
        }

        // Update fields
        if (request.getTitle() != null) property.setTitle(request.getTitle());
        if (request.getDescription() != null) property.setDescription(request.getDescription());
        if (request.getAddress() != null) property.setAddress(request.getAddress());
        if (request.getBasePrice() != null) property.setBasePrice(request.getBasePrice());
        if (request.getAmenities() != null) property.setAmenities(request.getAmenities());
        if (request.getAvailableFrom() != null) property.setAvailableFrom(request.getAvailableFrom());

        Property updatedProperty = propertyRepository.save(property);
        return convertToResponse(updatedProperty);
    }

    public void deleteProperty(Long id, UserPrincipal userPrincipal) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        // Check ownership or admin role
        if (!property.getOwner().getId().equals(userPrincipal.getId()) &&
                userPrincipal.getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new BadRequestException("You can only delete your own properties");
        }

//        property.setStatus(PropertyStatus.INACTIVE); is soft delete
//        propertyRepository.save(property);
        propertyRepository.delete(property);
        log.info("Hard-deleted property: {} by user: {}", id, userPrincipal.getId());
        log.info("Deleted property: {} by user: {}", id, userPrincipal.getId());
    }

    public UnitResponse addUnit(Long propertyId, CreateUnitRequest request, Long ownerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You can only add units to your own properties");
        }

        Unit unit = Unit.builder()
                .name(request.getName())
                .type(request.getType())
                .capacity(request.getCapacity())
                .bedroomCount(request.getBedroomCount())
                .bathroomCount(request.getBathroomCount())
                .monthlyRent(request.getMonthlyRent())
                .securityDeposit(request.getSecurityDeposit())
                .description(request.getDescription())
                .amenities(request.getAmenities())
                .property(property)
                .build();

        Unit savedUnit = unitRepository.save(unit);
        return convertToUnitResponse(savedUnit);
    }

    public Page<PropertyResponse> getPropertiesByOwner(Long ownerId, Pageable pageable) {
        return propertyRepository.findByOwnerId(ownerId, pageable)
                .map(this::convertToResponse);
    }

    private PropertyResponse convertToResponse(Property property) {
        Double averageRating = reviewRepository.getAverageRatingByPropertyId(property.getId());
        Long reviewCount = reviewRepository.getReviewCountByPropertyId(property.getId());

        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .address(property.getAddress())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .basePrice(property.getBasePrice())
                .currency(property.getCurrency())
                .photos(property.getPhotos())
                .amenities(property.getAmenities())
                .availableFrom(property.getAvailableFrom())
                .status(property.getStatus().name())
                .featured(property.getFeatured())
                .viewCount(property.getViewCount())
                .owner(PropertyResponse.OwnerInfo.builder()
                        .id(property.getOwner().getId())
                        .fullName(property.getOwner().getFullName())
                        .phone(property.getOwner().getPhone())
                        .profilePictureUrl(property.getOwner().getProfilePictureUrl())
                        .build())
                .units(property.getUnits() != null ?
                        property.getUnits().stream()
                                .map(this::convertToUnitResponse)
                                .collect(Collectors.toList()) : null)
                .reviewSummary(PropertyResponse.ReviewSummary.builder()
                        .averageRating(averageRating)
                        .totalReviews(reviewCount.intValue())
                        .build())
                .createdAt(property.getCreatedAt())
                .build();
    }

    private UnitResponse convertToUnitResponse(Unit unit) {
        return UnitResponse.builder()
                .id(unit.getId())
                .name(unit.getName())
                .type(unit.getType())
                .capacity(unit.getCapacity())
                .bedroomCount(unit.getBedroomCount())
                .bathroomCount(unit.getBathroomCount())
                .monthlyRent(unit.getMonthlyRent())
                .securityDeposit(unit.getSecurityDeposit())
                .available(unit.getAvailable())
                .description(unit.getDescription())
                .amenities(unit.getAmenities())
                .build();
    }

    public PropertyResponse verifyProperty(Long id) {
        return null;
    }

    public UnitResponse updateUnit(Long propertyId, Long unitId, @Valid CreateUnitRequest request, Long id) {
       //TODO

        return null;
    }

    public void deleteUnit(Long propertyId, Long unitId, Long id) {
        //TODO
    }
}