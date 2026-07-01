package com.example.flood_alert.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.User;
import com.example.flood_alert.entity.UserFcmToken;
import com.example.flood_alert.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    UserFcmTokenRepository userFcmTokenRepository;
    AuthenticationService authenticationService;

    // lưu FCM token
    @Transactional
    public void saveToken(String token) {

        if (userFcmTokenRepository.existsByToken(token)) {
            return;
        }

        User user = authenticationService.getCurrentUser();

        UserFcmToken userToken = UserFcmToken.builder()
                .user(user)
                .token(token)
                .createdAt(LocalDateTime.now())
                .build();

        userFcmTokenRepository.save(userToken);
    }

    // Method gửi 1 token
    public void sendNotification(String token, String title, String body)
            throws FirebaseMessagingException {

        Message message = Message.builder()
                .setToken(token)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                .build();

        FirebaseMessaging.getInstance().send(message);
    }
}
