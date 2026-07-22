package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.response.FloodAlertResponse;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.FloodAlert;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.enums.StatusAlert;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.FloodAlertRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AlertService {
    UserRepository userRepository;
    FloodAlertRepository floodAlertRepository;
    EmailProcessor emailProcessor;
    WebPushProcessor webPushProcessor;
    AreaRiskSnapshotRepository areaRiskSnapshotRepository;

    private static final long ALERT_COOLDOW_SECONDS = 30;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSnapshot(UUID snapshotId) {
        AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                .findByIdWithArea(snapshotId)                
                .orElseThrow(() -> new AppException(ErrorCode.SNAPSHOT_NOT_FOUND));

        log.info("PROCESS ALERT area={} risk={}",
                snapshot.getArea().getId(),
                snapshot.getRiskLevel());
        RiskLevel riskLevel = snapshot.getRiskLevel();

        log.info("1. Pass LOW");

        if (riskLevel == RiskLevel.LOW) {
            return;
        }

        boolean allow = shouldSendAlert(snapshot);
        log.info("2. shouldSendAlert={}", allow);

        if (!allow) {
            return;
        }

        List<User> users = userRepository.findByAreaAndTrangthai(snapshot.getArea(), Status.ACTIVE);

        log.info("3. users={}", users.size());

        if (users.isEmpty()) {
            return;
        }

        log.info("4. Before create alerts");

        switch (riskLevel) {
            case MEDIUM -> createMediumAlerts(snapshot, users);
            case HIGH -> createHighAlerts(snapshot, users);
            default -> {
            }
        }
        log.info("5. Finished create alerts");
    }

    private void createHighAlerts(AreaRiskSnapshot snapshot, List<User> users) {
        List<FloodAlert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (User user : users) {
            alerts.add(
                    FloodAlert.builder()
                            .snapshot(snapshot)
                            .user(user)
                            .area(snapshot.getArea())
                            .riskLevel(RiskLevel.HIGH)
                            .title(buildTitle(snapshot.getRiskLevel()))
                            .message(buildMessage(snapshot))
                            .channel(Channel.WEB_PUSH)
                            .status(StatusAlert.PENDING)
                            .createdAt(now)
                            .build());

            alerts.add(
                    FloodAlert.builder()
                            .snapshot(snapshot)
                            .user(user)
                            .area(snapshot.getArea())
                            .riskLevel(RiskLevel.HIGH)
                            .title(buildTitle(snapshot.getRiskLevel()))
                            .message(buildMessage(snapshot))
                            .channel(Channel.EMAIL)
                            .status(StatusAlert.PENDING)
                            .createdAt(now)
                            .build());
        }

        floodAlertRepository.saveAll(alerts);

        // Gửi email
        try {
            emailProcessor.processPendingEmails();
        } catch (Exception ex) {
            log.error("Email processing failed", ex);
            throw ex; // giữ nguyên hành vi rollback để không che giấu bug thật
        }
        // Gửi web push
        try {
            webPushProcessor.processPendingPushNotifications();
        } catch (Exception ex) {
            log.error("Push processing failed", ex);
            throw ex; // giữ nguyên hành vi rollback để không che giấu bug thật
        }
    }

    private void createMediumAlerts(AreaRiskSnapshot snapshot, List<User> users) {
        List<FloodAlert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (User user : users) {
            alerts.add(
                    FloodAlert.builder()
                            .snapshot(snapshot)
                            .user(user)
                            .area(snapshot.getArea())
                            .riskLevel(RiskLevel.MEDIUM)
                            .title(buildTitle(snapshot.getRiskLevel()))
                            .message(buildMessage(snapshot))
                            .channel(Channel.WEB_PUSH)
                            .status(StatusAlert.PENDING)
                            .createdAt(now)
                            .build());
        }
        log.info("Create {} alerts", alerts.size());

        floodAlertRepository.saveAll(alerts);

        log.info("Save done. Web push");
        // Gửi web push
        webPushProcessor.processPendingPushNotifications();
    }

    private String buildMessage(AreaRiskSnapshot snapshot) {
        String areaName = snapshot.getArea().getTenkhuvuc();

        return String.format(
                """
                        Khu vực %s đang ở mức cảnh báo %s.

                        Tỷ lệ dữ liệu nguy hiểm: %.1f%%
                        Số phút nguy hiểm liên tục: %d phút

                        Người dân nên theo dõi tình hình và chuẩn bị phương án ứng phó khi cần thiết.
                            """,
                areaName,
                riskText(snapshot.getRiskLevel()),
                snapshot.getDangerPercent(),
                snapshot.getDangerDurationMinutes());
    }

    private String riskText(RiskLevel level) {
        return switch (level) {
            case LOW -> "THẤP";
            case MEDIUM -> "TRUNG BÌNH";
            case HIGH -> "CAO";
        };
    }

    private String buildTitle(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case MEDIUM -> "Cảnh báo lũ mức TRUNG BÌNH";
            case HIGH -> "Cảnh báo lũ mức CAO";
            default -> "Thông báo";
        };
    }

    // Check chống spam alert
    private boolean shouldSendAlert(AreaRiskSnapshot snapshot) {
        log.info("CHECK COOLDOWN");

        Optional<FloodAlert> latestAlertOpt = floodAlertRepository
                .findTopByAreaOrderByCreatedAtDesc(snapshot.getArea());

        log.info("Found latest={}", latestAlertOpt.isPresent());

        if (latestAlertOpt.isEmpty()) {
            return true;
        }

        FloodAlert latestAlert = latestAlertOpt.get();

        RiskLevel currentRisk = snapshot.getRiskLevel();
        RiskLevel previousRisk = latestAlert.getRiskLevel();

        log.info("Latest created={}", latestAlert.getCreatedAt());
        log.info("Now={}", LocalDateTime.now());
        log.info("Current risk={}", currentRisk);
        log.info("Previous risk={}", previousRisk);

        // MEDIUM -> HIGH
        if (currentRisk.ordinal() > previousRisk.ordinal()) {
            log.info("Risk upgraded -> allow");
            return true;
        }

        // HIGH -> HIGH hoặc MEDIUM -> MEDIUM
        if (currentRisk == previousRisk) {
            boolean allow = latestAlert.getCreatedAt()
                    .plusSeconds(ALERT_COOLDOW_SECONDS)
                    .isBefore(LocalDateTime.now());

            log.info("Allow send={}", allow);

            return allow;
        }

        // HIGH -> MEDIUM
        log.info("Risk downgraded -> block");

        return false;
    }

    public Page<FloodAlertResponse> getAlertsByUser(
            UUID userId,
            Pageable pageable) {

        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return floodAlertRepository
                .findByUserOrderByCreatedAtDesc(
                        user,
                        pageable)
                .map(alert -> FloodAlertResponse.builder()
                        .tenkhuvuc(
                                alert.getArea().getTenkhuvuc())
                        .riskLevel(
                                alert.getRiskLevel())
                        .channel(
                                alert.getChannel())
                        .status(
                                alert.getStatus())
                        .createdAt(
                                alert.getCreatedAt())
                        .build());
    }
}
