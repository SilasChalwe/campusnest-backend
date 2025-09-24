
package com.nextinnomind.campusnestbackend.repository;

import com.nextinnomind.campusnestbackend.entity.BookingRequest;
import com.nextinnomind.campusnestbackend.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    Page<BookingRequest> findByStudentId(Long studentId, Pageable pageable);
    Page<BookingRequest> findByPropertyOwnerId(Long ownerId, Pageable pageable);
    Page<BookingRequest> findByStatus(BookingStatus status, Pageable pageable);
    List<BookingRequest> findByPropertyId(Long propertyId);
    List<BookingRequest> findByUnitId(Long unitId);

    @Query("SELECT br FROM BookingRequest br WHERE br.unit.id = :unitId " +
            "AND br.status = 'APPROVED' " +
            "AND ((br.startDate BETWEEN :startDate AND :endDate) " +
            "OR (br.endDate BETWEEN :startDate AND :endDate) " +
            "OR (br.startDate <= :startDate AND br.endDate >= :endDate))")
    List<BookingRequest> findConflictingBookings(@Param("unitId") Long unitId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}