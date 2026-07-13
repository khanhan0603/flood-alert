package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.response.PopupNotificationResponse;
import com.example.flood_alert.entity.Notification;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.entity.UserFcmToken;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.NotificationType;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.NotificationMapper;
import com.example.flood_alert.repository.NotificationRepository;
import com.example.flood_alert.repository.UserFcmTokenRepository;
import com.example.flood_alert.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    UserFcmTokenRepository userFcmTokenRepository;
    AuthenticationService authenticationService;
    NotificationRepository notificationRepository;
    UserRepository userRepository;
    NotificationMapper notificationMapper;

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
    public String sendNotification(
            String token,
            String title,
            String body,
            Map<String, String> data)
            throws FirebaseMessagingException {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("FCM token is null or empty");
        }

        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        String messageId = FirebaseMessaging.getInstance().send(builder.build());

        log.info("Send FCM success. messageId={}", messageId);

        return messageId;
    }

    @Transactional
    public void sendNewSosNotification(User leader, SosRequest sos) {

        // Không có Team Leader
        if (leader == null) {
            log.warn("Không thể gửi thông báo SOS vì Team Leader = null");
            return;
        }

        // Lấy tất cả FCM Token của Team Leader
        List<UserFcmToken> userTokens = userFcmTokenRepository.findByUserId(leader.getId());

        if (userTokens.isEmpty()) {
            log.warn("Team Leader {} chưa đăng ký FCM Token", leader.getId());
            return;
        }

        String title = "🚨 Có yêu cầu cứu hộ mới";

        String body = String.format(
                "Khu vực: %s | %d nạn nhân | Ưu tiên: %s",
                sos.getArea().getTenkhuvuc(),
                sos.getVictimCount(),
                sos.getPriority());

        // Data gửi kèm để FE xử lý khi click notification
        Map<String, String> data = Map.of(
                "type", "NEW_SOS",
                "sosId", sos.getId().toString(),
                "priority", sos.getPriority().name());

        for (UserFcmToken userToken : userTokens) {

            try {

                sendNotification(
                        userToken.getToken(),
                        title,
                        body,
                        data);

            } catch (FirebaseMessagingException ex) {

                log.error(
                        "Không thể gửi thông báo tới token {} của Team Leader {}",
                        userToken.getToken(),
                        leader.getId(),
                        ex);
            }
        }

        log.info(
                "Đã gửi thông báo SOS {} tới Team Leader {} ({} thiết bị)",
                sos.getId(),
                leader.getId(),
                userTokens.size());
    }

    @Transactional
    public void createCallWorkflowFailedNotifications(SosRequest sos) {

        RescueTeam team = sos.getTeam();

        UUID areaId = sos.getArea().getParent().getId();

        Set<User> receivers = new LinkedHashSet<>();

        // Team Leader
        if (team.getLeader() != null) {
            receivers.add(team.getLeader());
        }

        // Deputy Leader
        if (team.getDeputyLeader() != null) {
            receivers.add(team.getDeputyLeader());
        }

        // Province Operator
        List<User> provinceOperators = userRepository.findByRoleAndArea_IdAndTrangthai(
                Role.PROVINCE_OPERATOR,
                areaId,
                Status.ACTIVE);

        receivers.addAll(provinceOperators);

        if (receivers.isEmpty()) {
            return;
        }

        String title = "🚨 Không có người nhận điều phối SOS";

        String message = String.format(
                """
                        SOS %s tại %s (Ưu tiên: %s) không có Team Leader, Deputy Leader hoặc Province Operator nào xác nhận nhận điều phối.

                        Vui lòng liên hệ Hotline %s để xử lý khẩn cấp.
                        """,
                sos.getTrackingCode(),
                sos.getArea().getTenkhuvuc(),
                sos.getPriority(),
                team.getEmergencyPhone());

        for (User receiver : receivers) {

            // Popup
            createNotification(
                    receiver,
                    title,
                    message,
                    NotificationType.CALL_WORKFLOW_FAILED,
                    Channel.POPUP,
                    sos);

            createNotification(
                    receiver,
                    title,
                    message,
                    NotificationType.CALL_WORKFLOW_FAILED,
                    Channel.WEB_PUSH,
                    sos);

            createNotification(
                    receiver,
                    title,
                    message,
                    NotificationType.CALL_WORKFLOW_FAILED,
                    Channel.EMAIL,
                    sos);
        }
    }

    // Hàm tạo noti cho failed SOS
    private void createNotification(
            User user,
            String title,
            String message,
            NotificationType type,
            Channel channel,
            SosRequest sos) {

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .type(type)
                .channel(channel)
                .status(StatusAlert.PENDING)
                .user(user)
                .sos(sos)
                .build();

        notificationRepository.save(notification);
    }

    // Trả cho FE các thông tin popup của người đã đăng nhập
    @Transactional(readOnly = true)
    public List<PopupNotificationResponse> getMyPopupNotifications() {

        User currentUser = authenticationService.getCurrentUser();

        return notificationRepository
                .findPendingPopupNotifications(currentUser.getId())
                .stream()
                .map(notificationMapper::toPopupResponse)
                .toList();
    }

    // Đánh dấu đã đọc popup
    @Transactional
    public void markAsRead(UUID notificationId) {

        User currentUser = authenticationService.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // Không cho đọc notification của người khác
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.setStatus(StatusAlert.SENT);

        notificationRepository.save(notification);
    }
}
