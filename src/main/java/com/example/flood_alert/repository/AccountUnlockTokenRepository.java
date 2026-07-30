package com.example.flood_alert.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.AccountUnlockToken;

public interface AccountUnlockTokenRepository
        extends JpaRepository<AccountUnlockToken, UUID> {

    Optional<AccountUnlockToken> findByUserIdAndOtp(UUID userId, String otp);

    void deleteByExpiredAtBefore(LocalDateTime time);

    void deleteByUserId(UUID userId);
}