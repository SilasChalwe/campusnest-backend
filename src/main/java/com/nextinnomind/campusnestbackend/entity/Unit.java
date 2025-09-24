
package com.nextinnomind.campusnestbackend.entity;

import com.nextinnomind.campusnestbackend.enums.UnitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "units")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType type;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer bedroomCount;

    @Column(nullable = false)
    private Integer bathroomCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    private BigDecimal securityDeposit;

    @Column(nullable = false)
    @Builder.Default
    private Boolean available = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "unit_amenities", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "amenity")
    private List<String> amenities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL)
    private List<BookingRequest> bookingRequests;
}