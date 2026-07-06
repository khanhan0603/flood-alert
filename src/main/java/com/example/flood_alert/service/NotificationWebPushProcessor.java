package com.example.flood_alert.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.Notification;
import com.example.flood_alert.entity.UserFcmToken;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.repository.NotificationRepository;
import com.example.flood_alert.repository.UserFcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

// Class này tạo push web thông báo cho các quá trình làm nhiệm vụ như báo thất bại đến team leader...
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationWebPushProcessor {

    NotificationRepository notificationRepository;
    UserFcmTokenRepository userFcmTokenRepository;
    NotificationService notificationService;

    @Transactional
    public void processPendingPushNotifications() {

        List<Notification> notifications = notificationRepository.findByChannelAndStatus(
                Channel.WEB_PUSH,
                StatusAlert.PENDING);

        for (Notification notification : notifications) {

            List<UserFcmToken> userTokens = userFcmTokenRepository.findByUserId(
                    notification.getUser().getId());

            if (userTokens.isEmpty()) {
                notification.setStatus(StatusAlert.FAILED);
                continue;
            }

            // Data gửi kèm cho FE xử lý khi người dùng bấm notification
            Map<String, String> data = new HashMap<>();

            // Loại thông báo
            data.put("type", notification.getType().name());

            // SOS liên quan (nếu có)
            if (notification.getSos() != null) {
                data.put(
                        "sosId",
                        notification.getSos().getId().toString());
            }

            // Assignment liên quan (nếu có)
            if (notification.getAssignment() != null) {
                data.put(
                        "assignmentId",
                        notification.getAssignment().getId().toString());
            }

            // Yêu cầu chi viện liên quan (nếu có)
            if (notification.getSupportRequest() != null) {
                data.put(
                        "supportRequestId",
                        notification.getSupportRequest().getId().toString());
            }

            boolean success = false;

            for (UserFcmToken token : userTokens) {

                try {

                    notificationService.sendNotification(
                            token.getToken(),
                            notification.getTitle(),
                            notification.getMessage(),
                            data);

                    success = true;

                } catch (FirebaseMessagingException ex) {

                    log.error(
                            "Không gửi được Web Push Notification {}",
                            notification.getId(),
                            ex);
                }
            }

            notification.setStatus(
                    success
                            ? StatusAlert.SENT
                            : StatusAlert.FAILED);
        }

        notificationRepository.saveAll(notifications);
    }
}