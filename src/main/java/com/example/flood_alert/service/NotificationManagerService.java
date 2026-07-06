package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.Notification;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.NotificationType;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.repository.NotificationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationManagerService {

        NotificationRepository notificationRepository;

        NotificationEmailProcessor notificationEmailProcessor;

        NotificationWebPushProcessor notificationWebPushProcessor;

        // notify assignment failed to team leader
        @Transactional
        public void notifyAssignmentFailed(
                        User teamLeader,
                        SosAssignment assignment) {

                String title = "Nhóm cứu hộ không thể tiếp tục nhiệm vụ";

                String message = String.format(
                                """
                                                Nhóm %s đã báo thất bại khi thực hiện nhiệm vụ.

                                                Lý do: %s

                                                Vui lòng phân công nhóm khác hoặc gửi yêu cầu chi viện.
                                                """,
                                assignment.getGroup().getName(),
                                assignment.getFailedReason());

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.ASSIGNMENT_FAILED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .assignment(assignment)
                                                .sos(assignment.getSos())
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.ASSIGNMENT_FAILED)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .assignment(assignment)
                                                .sos(assignment.getSos())
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build());

                notificationRepository.saveAll(notifications);

                // Gửi email
                notificationEmailProcessor.processPendingEmails();

                // Gửi Web Push
                notificationWebPushProcessor.processPendingPushNotifications();
        }
}