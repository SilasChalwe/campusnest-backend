
package com.nextinnomind.campusnestbackend.repository;

import com.nextinnomind.campusnestbackend.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<Unit> findByPropertyId(Long propertyId);
    List<Unit> findByPropertyIdAndAvailableTrue(Long propertyId);

    @Query("SELECT u FROM Unit u WHERE u.property.id = :propertyId AND u.available = true " +
            "AND u.id NOT IN (SELECT br.unit.id FROM BookingRequest br WHERE br.status = 'APPROVED')")
    List<Unit> findAvailableUnitsByPropertyId(@Param("propertyId") Long propertyId);
}