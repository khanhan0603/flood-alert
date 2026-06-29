package com.example.flood_alert.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.InvalidatedToken;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, UUID> {

    boolean existsByJwtId(String jwtId);

    //Dọn token blacklist
    void deleteByExpiryTimeBefore(LocalDateTime time);

}
