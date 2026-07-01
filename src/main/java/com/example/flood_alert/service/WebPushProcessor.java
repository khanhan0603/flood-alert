package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.FloodAlert;
import com.example.flood_alert.entity.UserFcmToken;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.repository.FloodAlertRepository;
import com.example.flood_alert.repository.UserFcmTokenRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebPushProcessor {
    FloodAlertRepository floodAlertRepository; // lấy các alert PENDING
    UserFcmTokenRepository userFcmTokenRepository; // lấy FCM token của user.
    NotificationService notificationService; // gửi notification qua Firebase.

    @Transactional
    public void processPendingPushNotifications() {
        // lấy danh sách alert PENDING
        List<FloodAlert> alerts = floodAlertRepository.findByChannelAndStatus(
                Channel.WEB_PUSH,
                StatusAlert.PENDING);
        // Duyệt từng alert
        for (FloodAlert alert : alerts) {

            // lấy FCM token của user cho từng alert

            List<UserFcmToken> tokens = userFcmTokenRepository.findByUser(alert.getUser());

            if (tokens.isEmpty()) {

                log.warn(
                        "No FCM token for user {}",
                        alert.getUser().getId());

                alert.setStatus(StatusAlert.FAILED);

                continue;
            }

            try {

                for (UserFcmToken token : tokens) {

                    notificationService.sendNotification(
                            token.getToken(),
                            alert.getTitle(),
                            alert.getMessage());
                }

                alert.setStatus(StatusAlert.SENT);
                alert.setSentAt(LocalDateTime.now());

            } catch (Exception e) {

                log.error(
                        "Push notification failed alert={}",
                        alert.getId(),
                        e);

                alert.setStatus(StatusAlert.FAILED);
            }
        }

        floodAlertRepository.saveAll(alerts);
    }
}
