package com.example.flood_alert.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.Notification;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.repository.NotificationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

// Class này tạo email thông báo cho các quá trình làm nhiệm vụ như báo thất bại đến team leader...
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationEmailProcessor {

    NotificationRepository notificationRepository;
    EmailService emailService;

    @Transactional
    public void processPendingEmails() {

        List<Notification> notifications =
                notificationRepository.findByChannelAndStatus(
                        Channel.EMAIL,
                        StatusAlert.PENDING);

        for (Notification notification : notifications) {

            try {

                emailService.sendEmail(
                        notification.getUser().getEmail(),
                        notification.getTitle(),
                        notification.getMessage());

                notification.setStatus(StatusAlert.SENT);

            } catch (Exception ex) {

                log.error(
                        "Không gửi được email notification {}",
                        notification.getId(),
                        ex);

                notification.setStatus(StatusAlert.FAILED);
            }
        }

        notificationRepository.saveAll(notifications);
    }
}