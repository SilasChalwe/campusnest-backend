
package com.nextinnomind.campusnestbackend.repository;

import com.nextinnomind.campusnestbackend.entity.Payment;
import com.nextinnomind.campusnestbackend.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByStudentId(Long studentId, Pageable pageable);
    List<Payment> findByBookingId(Long bookingId);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.student.id = :studentId AND p.status = 'COMPLETED'")
    BigDecimal getTotalAmountPaidByStudent(@Param("studentId") Long studentId);

    @Query("SELECT p FROM Payment p WHERE p.booking.property.owner.id = :landlordId AND p.status = 'COMPLETED'")
    Page<Payment> findCompletedPaymentsByLandlord(@Param("landlordId") Long landlordId, Pageable pageable);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.booking.property.owner.id = :landlordId AND p.status = 'COMPLETED' " +
            "AND p.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalEarningsByLandlord(@Param("landlordId") Long landlordId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}