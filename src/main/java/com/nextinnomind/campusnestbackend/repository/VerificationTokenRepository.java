package com.nextinnomind.campusnestbackend.repository;

import com.nextinnomind.campusnestbackend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByUserIdAndTypeAndUsedFalse(Long userId, String type);

    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.user.id = :userId AND vt.type = :type AND vt.used = false")
    void invalidateUserTokensByType(@Param("userId") Long userId, @Param("type") String type);

    // Optional: cleanup method for expired tokens
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}