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
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebPushProcessor {

    FloodAlertRepository floodAlertRepository;
    UserFcmTokenRepository userFcmTokenRepository;
    NotificationService notificationService;

    @Transactional
    public void processPendingPushNotifications() {

        List<FloodAlert> alerts = floodAlertRepository.findByChannelAndStatus(
                Channel.WEB_PUSH,
                StatusAlert.PENDING);

        for (FloodAlert alert : alerts) {

            List<UserFcmToken> tokens =
                    userFcmTokenRepository.findByUser(alert.getUser());

            if (tokens.isEmpty()) {

                log.warn(
                        "No FCM token for user {}",
                        alert.getUser().getId());

                alert.setStatus(StatusAlert.FAILED);
                continue;
            }

            boolean sent = false;

            for (UserFcmToken token : tokens) {

                try {

                    notificationService.sendNotification(
                            token.getToken(),
                            alert.getTitle(),
                            alert.getMessage());

                    sent = true;

                } catch (FirebaseMessagingException e) {

                    log.error(
                            "Push failed. user={} token={}",
                            alert.getUser().getId(),
                            token.getId(),
                            e);

                    // Token hết hạn hoặc không còn hợp lệ
                    if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {

                        log.warn(
                                "Delete invalid FCM token {}",
                                token.getId());

                        userFcmTokenRepository.delete(token);
                    }
                }
            }

            if (sent) {

                alert.setStatus(StatusAlert.SENT);
                alert.setSentAt(LocalDateTime.now());

            } else {

                alert.setStatus(StatusAlert.FAILED);
            }
        }

        floodAlertRepository.saveAll(alerts);
    }
}