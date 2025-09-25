package com.nextinnomind.campusnestbackend.controller;

import com.nextinnomind.campusnestbackend.dto.common.ApiResponse;
import com.nextinnomind.campusnestbackend.dto.property.*;
import com.nextinnomind.campusnestbackend.security.CurrentUser;
import com.nextinnomind.campusnestbackend.security.UserPrincipal;
import com.nextinnomind.campusnestbackend.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Property management APIs")
public class PropertyController {

    private final PropertyService propertyService;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Create new property")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> createProperty(
            @CurrentUser UserPrincipal userPrincipal,
            @ModelAttribute @Valid CreatePropertyRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        List<PropertyResponse> createdProperties = new ArrayList<>();

        // ---------- AUTO-CREATE 5 TEST PROPERTIES ----------
        for (int i = 1; i <= 5; i++) {
            CreatePropertyRequest testRequest = new CreatePropertyRequest();
            testRequest.setTitle("Test Property " + i + " - " + System.currentTimeMillis());
            testRequest.setDescription("This is auto-generated property #" + i + " for testing.");
            testRequest.setAddress("123 Test Lane #" + i);
            testRequest.setLatitude(-15.4167 + i * 0.001);
            testRequest.setLongitude(28.2833 + i * 0.001);
            testRequest.setBasePrice(BigDecimal.valueOf(500 + i * 100));
            testRequest.setCurrency("USD");
            testRequest.setAmenities(List.of("WiFi", "Parking", "Pool"));
            testRequest.setAvailableFrom(java.time.LocalDate.now());

            // images can be empty for testing
            List<MultipartFile> testImages = images != null ? images : new ArrayList<>();

            PropertyResponse response = propertyService.createProperty(testRequest, testImages, userPrincipal.getId());
            createdProperties.add(response);
        }
        // ----------------------------------------------------

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("5 test properties created successfully", createdProperties));
    }

    @GetMapping
    @Operation(summary = "Search and filter properties")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> searchProperties(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice, // change to BigDecimal
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false) List<String> amenities,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<PropertyResponse> properties = propertyService.searchProperties(
                query, address, lat, lng, radiusKm, minPrice, maxPrice, amenities, pageable);

        return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get property details")
    public ResponseEntity<ApiResponse<PropertyResponse>> getProperty(@PathVariable Long id) {
        PropertyResponse response = propertyService.getPropertyById(id);
        return ResponseEntity.ok(ApiResponse.success("Property retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Update property")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePropertyRequest request) {
        PropertyResponse response = propertyService.updateProperty(id, request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LANDLORD') or hasRole('ADMIN')")
    @Operation(summary = "Delete property")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id) {
        propertyService.deleteProperty(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully", null));
    }

    @GetMapping("/my-properties")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Get landlord's properties")
    public ResponseEntity<ApiResponse<Page<PropertyResponse>>> getMyProperties(
            @CurrentUser UserPrincipal userPrincipal,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PropertyResponse> properties = propertyService.getPropertiesByOwner(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Properties retrieved successfully", properties));
    }

    @PostMapping("/{id}/units")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Add unit to property")
    public ResponseEntity<ApiResponse<UnitResponse>> addUnit(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody CreateUnitRequest request) {
        UnitResponse response = propertyService.addUnit(id, request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Unit added successfully", response));
    }

    @PutMapping("/{propertyId}/units/{unitId}")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Update unit")
    public ResponseEntity<ApiResponse<UnitResponse>> updateUnit(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long propertyId,
            @PathVariable Long unitId,
            @Valid @RequestBody CreateUnitRequest request) {
        UnitResponse response = propertyService.updateUnit(propertyId, unitId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Unit updated successfully", response));
    }

    @DeleteMapping("/{propertyId}/units/{unitId}")
    @PreAuthorize("hasRole('LANDLORD')")
    @Operation(summary = "Delete unit")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long propertyId,
            @PathVariable Long unitId) {
        propertyService.deleteUnit(propertyId, unitId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Unit deleted successfully", null));
    }
}