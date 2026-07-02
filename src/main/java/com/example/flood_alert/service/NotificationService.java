package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.SosRequest;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
                        Notification.builder()
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
}
