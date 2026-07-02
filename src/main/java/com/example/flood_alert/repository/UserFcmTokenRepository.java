package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.User;
import com.example.flood_alert.entity.UserFcmToken;

public interface UserFcmTokenRepository
        extends JpaRepository<UserFcmToken, UUID> {

    List<UserFcmToken> findByUser(User user);

    List<UserFcmToken> findByUserId(UUID userId);

    boolean existsByToken(String token);
}