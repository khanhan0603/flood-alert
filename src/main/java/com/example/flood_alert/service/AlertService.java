package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
import com.example.flood_alert.repository.FloodAlertRepository;
import com.example.flood_alert.repository.UserRepository;

import jakarta.transaction.Transactional;
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

    private static final long ALERT_COOLDOWN_HOURS = 6;

    @Transactional
    public void processSnapshot(AreaRiskSnapshot snapshot) {
        RiskLevel riskLevel = snapshot.getRiskLevel();

        if (riskLevel == RiskLevel.LOW) {
            return;
        }

        if (!shouldSendAlert(snapshot)) {
            log.info(
                    "Skip duplicate alert for area {}",
                    snapshot.getArea().getId());
            return;
        }

        List<User> users = userRepository.findByAreaAndTrangthai(snapshot.getArea(), Status.ACTIVE);

        if (users.isEmpty()) {
            log.info("No active users found in area {}", snapshot.getArea().getId());
            return;
        }

        switch (riskLevel) {
            case MEDIUM -> createMediumAlerts(snapshot, users);
            case HIGH -> createHighAlerts(snapshot, users);
            default -> {
            }
        }
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

        //Gửi email
        emailProcessor.processPendingEmails();
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
        floodAlertRepository.saveAll(alerts);
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
        Optional<FloodAlert> latestAlertOpt = floodAlertRepository
                .findTopByAreaOrderByCreatedAtDesc(snapshot.getArea());

        if (latestAlertOpt.isEmpty()) {
            return true;
        }

        FloodAlert latestAlert = latestAlertOpt.get();

        // Lấy mức cảnh báo hiện tại
        RiskLevel currentRisk = snapshot.getRiskLevel();

        // Lấy mức cảnh báo trước đó
        RiskLevel previousRisk = latestAlert.getRiskLevel();

        // MEDIUM -> HIGH
        if (currentRisk.ordinal() > previousRisk.ordinal()) {
            return true;
        }

        // HIGH -> HIGH hoặc MEDIUM -> MEDIUM trong 6 tiếng
        if (currentRisk == previousRisk) {
            return latestAlert.getCreatedAt()
                    .plusHours(ALERT_COOLDOWN_HOURS)
                    .isBefore(LocalDateTime.now());
        }
        // HIGH->MEDIUM
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
