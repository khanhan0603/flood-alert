package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.Notification;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.entity.SupportRequestItem;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.NotificationType;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.repository.NotificationRepository;
import com.example.flood_alert.repository.UserRepository;

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
        UserRepository userRepository;

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

        // Thông báo đến team leader là group trong team cần support
        @Transactional
        public void notifyGroupSupportRequest(
                        User teamLeader,
                        SupportRequest supportRequest) {

                String title = "Nhóm cứu hộ yêu cầu hỗ trợ";

                String message = String.format(
                                """
                                                Nhóm cứu hộ đã gửi yêu cầu hỗ trợ cho nhiệm vụ cứu hộ.

                                                Lý do:
                                                %s

                                                Vui lòng xem chi tiết và quyết định điều phối thêm nhóm hoặc gửi yêu cầu chi viện lên cấp tỉnh.
                                                """,
                                supportRequest.getReason());

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SUPPORT_REQUEST_CREATED)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(supportRequest)
                                                .sos(supportRequest.getSos())
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SUPPORT_REQUEST_CREATED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(supportRequest)
                                                .sos(supportRequest.getSos())
                                                .build());

                notificationRepository.saveAll(notifications);

                notificationEmailProcessor.processPendingEmails();

                notificationWebPushProcessor.processPendingPushNotifications();
        }

        // Thông báo gửi support request của team leader đến province_operator
        @Transactional
        public void notifyProvinceSupportRequest(
                        SupportRequest supportRequest) {

                // Lấy parent (tỉnh) từ sos trong support request
                Area province = supportRequest.getSos()
                                .getArea()
                                .getParent();

                List<User> provinceOperators = userRepository.findByRoleAndArea_IdAndTrangthai(
                                Role.PROVINCE_OPERATOR,
                                province.getId(),
                                Status.ACTIVE);

                if (provinceOperators.isEmpty()) {
                        return;
                }

                String title = "Đội cứu hộ yêu cầu chi viện";

                String message = String.format(
                                """
                                                Đội cứu hộ tại khu vực %s đã gửi yêu cầu chi viện.

                                                Lý do: %s

                                                Vui lòng xem xét và điều phối lực lượng hỗ trợ.
                                                """,
                                supportRequest.getSos().getArea().getTenkhuvuc(),
                                supportRequest.getReason());

                List<Notification> notifications = new ArrayList<>();

                for (User user : provinceOperators) {

                        notifications.add(
                                        Notification.builder()
                                                        .user(user)
                                                        .title(title)
                                                        .message(message)
                                                        .type(NotificationType.SUPPORT_REQUEST_CREATED)
                                                        .channel(Channel.EMAIL)
                                                        .status(StatusAlert.PENDING)
                                                        .supportRequest(supportRequest)
                                                        .sos(supportRequest.getSos())
                                                        .build());

                        notifications.add(
                                        Notification.builder()
                                                        .user(user)
                                                        .title(title)
                                                        .message(message)
                                                        .type(NotificationType.SUPPORT_REQUEST_CREATED)
                                                        .channel(Channel.WEB_PUSH)
                                                        .status(StatusAlert.PENDING)
                                                        .supportRequest(supportRequest)
                                                        .sos(supportRequest.getSos())
                                                        .build());
                }

                notificationRepository.saveAll(notifications);

                notificationEmailProcessor.processPendingEmails();

                notificationWebPushProcessor.processPendingPushNotifications();
        }

        // Thông báo Province đã xử lý xong Support Request của Team Leader
        @Transactional
        public void notifySupportRequestApproved(
                        SupportRequest supportRequest) {

                User teamLeader = supportRequest.getRequestedBy();

                String title = "Yêu cầu chi viện đã được xử lý";

                String message = """
                                Điều hành cấp tỉnh đã xử lý yêu cầu chi viện của đội.

                                Vui lòng xem chi tiết kết quả từng hạng mục hỗ trợ và các đội được điều động.
                                """;

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SUPPORT_REQUEST_APPROVED)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(supportRequest)
                                                .sos(supportRequest.getSos())
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SUPPORT_REQUEST_APPROVED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(supportRequest)
                                                .sos(supportRequest.getSos())
                                                .build());

                notificationRepository.saveAll(notifications);

                notificationEmailProcessor.processPendingEmails();

                notificationWebPushProcessor.processPendingPushNotifications();
        }

        // Thông báo Team Leader được Province điều động hỗ trợ
        @Transactional
        public void notifySupportAssignmentAssigned(
                        SupportRequestItem item) {

                // Team Leader của Team được điều động
                User teamLeader = item.getAssignedTeam().getLeader();

                if (teamLeader == null) {
                        return;
                }

                String title = "Đội của bạn được điều động hỗ trợ cứu hộ";

                String message = String.format("""
                                Điều hành cấp tỉnh đã điều động đội của bạn tham gia hỗ trợ cứu hộ.

                                Hạng mục hỗ trợ: %s

                                Vui lòng phân công nhóm phù hợp để thực hiện nhiệm vụ.
                                """,
                                item.getSupportType());

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SUPPORT_ASSIGNMENT_ASSIGNED)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(item.getSupportRequest())
                                                .sos(item.getSupportRequest().getSos())
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SUPPORT_ASSIGNMENT_ASSIGNED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(item.getSupportRequest())
                                                .sos(item.getSupportRequest().getSos())
                                                .build());

                notificationRepository.saveAll(notifications);

                notificationEmailProcessor.processPendingEmails();

                notificationWebPushProcessor.processPendingPushNotifications();
        }

        // Thông báo cho province Team được điều động từ chối hạng mục hỗ trợ
        @Transactional
        public void notifySupportAssignmentRejected(
                        SupportRequestItem item) {

                Area province = item.getSupportRequest()
                                .getSos()
                                .getArea()
                                .getParent();

                List<User> provinceOperators = userRepository
                                .findByRoleAndArea_IdAndTrangthai(
                                                Role.PROVINCE_OPERATOR,
                                                province.getId(),
                                                Status.ACTIVE);

                if (provinceOperators.isEmpty()) {
                        return;
                }

                String title = "Đội cứu hộ từ chối hạng mục hỗ trợ";

                String message = String.format("""
                                Đội %s đã từ chối thực hiện hạng mục hỗ trợ %s.

                                Lý do:
                                %s

                                Vui lòng điều phối đội khác.
                                """,
                                item.getAssignedTeam().getName(),
                                item.getSupportType(),
                                item.getTeamResponse());

                List<Notification> notifications = new ArrayList<>();

                for (User user : provinceOperators) {

                        notifications.add(
                                        Notification.builder()
                                                        .user(user)
                                                        .title(title)
                                                        .message(message)
                                                        .type(NotificationType.SUPPORT_ASSIGNMENT_REJECTED)
                                                        .channel(Channel.EMAIL)
                                                        .status(StatusAlert.PENDING)
                                                        .supportRequest(item.getSupportRequest())
                                                        .sos(item.getSupportRequest().getSos())
                                                        .build());

                        notifications.add(
                                        Notification.builder()
                                                        .user(user)
                                                        .title(title)
                                                        .message(message)
                                                        .type(NotificationType.SUPPORT_ASSIGNMENT_REJECTED)
                                                        .channel(Channel.WEB_PUSH)
                                                        .status(StatusAlert.PENDING)
                                                        .supportRequest(item.getSupportRequest())
                                                        .sos(item.getSupportRequest().getSos())
                                                        .build());
                }

                notificationRepository.saveAll(notifications);

                notificationEmailProcessor.processPendingEmails();

                notificationWebPushProcessor.processPendingPushNotifications();
        }

        // Thông báo đến group leader có nhiệm vụ hỗ trợ
        @Transactional
        public void notifyAssignmentAssigned(SosAssignment assignment) {

                User groupLeader = assignment.getGroup().getLeader();

                if (groupLeader == null) {
                        return;
                }

                String title = "Bạn được phân công nhiệm vụ hỗ trợ";

                String message = String.format("""
                                Nhóm %s được giao tham gia hỗ trợ cứu hộ.

                                Vai trò: %s

                                Vui lòng xác nhận và thực hiện nhiệm vụ.
                                """,
                                assignment.getGroup().getName(),
                                assignment.getRole());

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(groupLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SOS_ASSIGNED)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .assignment(assignment)
                                                .sos(assignment.getSos())
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(groupLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SOS_ASSIGNED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .assignment(assignment)
                                                .sos(assignment.getSos())
                                                .build());

                notificationRepository.saveAll(notifications);

                notificationEmailProcessor.processPendingEmails();
                notificationWebPushProcessor.processPendingPushNotifications();
        }
}