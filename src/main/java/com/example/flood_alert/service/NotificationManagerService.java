package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.Notification;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.SosRequest;
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

        @Transactional
        public void notifyNewSos(User teamLeader,SosRequest sos) {

                if (teamLeader == null) {
                        return;
                }

                String title = "🚨 Có yêu cầu cứu hộ mới";

                String message = String.format(
                                "Khu vực: %s | %d nạn nhân | Ưu tiên: %s",
                                sos.getArea().getTenkhuvuc(),
                                sos.getVictimCount(),
                                sos.getPriority());

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SOS_NEW)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .sos(sos)
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SOS_NEW)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .sos(sos)
                                                .build());

                notificationRepository.saveAll(notifications);

                notificationEmailProcessor.processPendingEmails();
                notificationWebPushProcessor.processPendingPushNotifications();
        }

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
                notificationWebPushProcessor.processPendingPushNotifications();
                notificationEmailProcessor.processPendingEmails();
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
                notificationWebPushProcessor.processPendingPushNotifications();
                notificationEmailProcessor.processPendingEmails();
        }

        // Thông báo cho người tạo Support Request khi không Province Operator nào phản
        // hồi
        @Transactional
        public void notifySupportRequestWorkflowFailed(
                        SupportRequest supportRequest) {

                User requester = supportRequest.getRequestedBy();

                String title = "Yêu cầu hỗ trợ chưa có người tiếp nhận";

                String message = """
                                Hiện chưa có Province Operator nào phản hồi yêu cầu hỗ trợ của bạn.

                                Hệ thống đã gửi thông báo đến các Province Operator để chủ động nhận điều phối.

                                Vui lòng theo dõi quá trình xử lý.
                                """;

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(requester)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.CALL_WORKFLOW_FAILED)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(supportRequest)
                                                .sos(supportRequest.getSos())
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(requester)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.CALL_WORKFLOW_FAILED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(supportRequest)
                                                .sos(supportRequest.getSos())
                                                .build());

                notificationRepository.saveAll(notifications);
                notificationWebPushProcessor.processPendingPushNotifications();
                notificationEmailProcessor.processPendingEmails();
        }

        // Gửi thông báo cho team leader đã có province nhận điều phối
        public void notifySupportRequestClaimed(SupportRequest supportRequest) {
                User teamLeader = supportRequest.getRequestedBy();

                String title = "Yêu cầu hỗ trợ đã được nhận điều phối";
                String message = String.format("""
                                 Lực lượng điều phối cấp tỉnh %s đã nhận điều phối yêu cầu hỗ trợ.

                                 Vui lòng theo dõi quá trình điều phối.
                                """, supportRequest.getRequestedBy().getHoten());

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SUPPORT_REQUEST_CLAIMED)
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
                                                .type(NotificationType.SUPPORT_REQUEST_CLAIMED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(supportRequest)
                                                .sos(supportRequest.getSos())
                                                .build());

                notificationRepository.saveAll(notifications);
                notificationWebPushProcessor.processPendingPushNotifications();
                notificationEmailProcessor.processPendingEmails();
        }

        // Thông báo đến team leader đã có người nhận điều phối yêu cầu cứu hộ
        @Transactional
        public void notifySosDispatcherClaimed(SosRequest sos) {

                User teamLeader = sos.getTeam().getLeader();

                if (teamLeader == null) {
                        return;
                }

                // Nếu là team leader nhận điều phối thì ko cần gửi cho team leader
                if (teamLeader.getId().equals(sos.getDispatcherUser().getId())) {
                        return;
                }

                String dispatcherName = sos.getDispatcherUser().getHoten();

                String dispatcherRole = switch (sos.getDispatcherType()) {
                        case TEAM_LEADER -> "Đội trưởng";
                        case DEPUTY_LEADER -> "Đội phó";
                        case PROVINCE_OPERATOR -> "Điều phối viên cấp tỉnh";
                };

                String title = "SOS đã có người nhận điều phối";

                String message = String.format(
                                "%s %s đã tiếp nhận điều phối yêu cầu cứu hộ.",
                                dispatcherRole,
                                dispatcherName);

                List<Notification> notifications = new ArrayList<>();

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SOS_ASSIGNED)
                                                .channel(Channel.EMAIL)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(null)
                                                .sos(sos)
                                                .build());

                notifications.add(
                                Notification.builder()
                                                .user(teamLeader)
                                                .title(title)
                                                .message(message)
                                                .type(NotificationType.SOS_ASSIGNED)
                                                .channel(Channel.WEB_PUSH)
                                                .status(StatusAlert.PENDING)
                                                .supportRequest(null)
                                                .sos(sos)
                                                .build());

                notificationRepository.saveAll(notifications);
                notificationWebPushProcessor.processPendingPushNotifications();
                notificationEmailProcessor.processPendingEmails();

        }
}