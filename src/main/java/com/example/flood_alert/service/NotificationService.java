package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.User;
import com.example.flood_alert.entity.UserFcmToken;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.UserFcmTokenRepository;
import com.example.flood_alert.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    UserRepository userRepository;
    UserFcmTokenRepository userFcmTokenRepository;

    @Transactional
    public void saveToken(UUID userId,String token) {

        if (userFcmTokenRepository.existsByToken(token)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserFcmToken userToken = UserFcmToken.builder()
                .user(user)
                .token(token)
                .createdAt(LocalDateTime.now())
                .build();

        userFcmTokenRepository.save(userToken);
    }
}
