package com.example.flood_alert.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.FloodPrediction;
import com.example.flood_alert.entity.PredictionAlert;
import com.example.flood_alert.enums.AlertStatus;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.repository.PredictionAlertRepository;
import com.example.flood_alert.repository.PredictionRepository;
import com.example.flood_alert.repository.RescueTeamRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PredictionAlertService {

        PredictionRepository predictionRepository;
        PredictionAlertRepository predictionAlertRepository;
        NotificationManagerService notificationManagerService;
        RescueTeamRepository rescueTeamRepository;

        public void createAlerts(UUID predictionJobHistoryId) {
                log.info("=== CREATE ALERT START ===");

                List<FloodPrediction> predictions = predictionRepository
                                .findHighRiskPredictionsByPredictionJobHistoryId(predictionJobHistoryId);

                log.info("Create alerts for job {}", predictionJobHistoryId);

                List<PredictionAlert> alerts = new ArrayList<>();

                for (FloodPrediction prediction : predictions) {

                        createLead1Alert(prediction, alerts);
                        createLead2Alert(prediction, alerts);
                        createLead3Alert(prediction, alerts);
                }

                if (alerts.isEmpty()) {
                        return;
                }

                predictionAlertRepository.saveAll(alerts);
                log.info("Saved {} alerts", alerts.size());

                for (PredictionAlert alert : alerts) {
                        log.info("Processing area {}", alert.getArea().getId());
                        rescueTeamRepository
                                        .findByArea_Id(alert.getArea().getId())
                                        .ifPresent(team -> {

                                                if (team.getLeader() != null) {

                                                        notificationManagerService.notifyHighRiskPrediction(
                                                                        team.getLeader(),
                                                                        alert);
                                                }
                                        });
                }
        }

        private void createLead1Alert(
                        FloodPrediction prediction,
                        List<PredictionAlert> alerts) {

                if (prediction.getLead1() == RiskLevel.HIGH) {
                        alerts.add(buildAlert(
                                        prediction,
                                        prediction.getLead1Date()));
                }
        }

        private void createLead2Alert(
                        FloodPrediction prediction,
                        List<PredictionAlert> alerts) {

                if (prediction.getLead2() == RiskLevel.HIGH) {
                        alerts.add(buildAlert(
                                        prediction,
                                        prediction.getLead2Date()));
                }
        }

        private void createLead3Alert(
                        FloodPrediction prediction,
                        List<PredictionAlert> alerts) {

                if (prediction.getLead3() == RiskLevel.HIGH) {
                        alerts.add(buildAlert(
                                        prediction,
                                        prediction.getLead3Date()));
                }
        }

        private PredictionAlert buildAlert(
                        FloodPrediction prediction,
                        Date predictionDate) {

                return PredictionAlert.builder()
                                .predictionJobHistory(prediction.getPredictionJobHistory())
                                .area(prediction.getArea())
                                .predictionDate(predictionDate)
                                .riskLevel(RiskLevel.HIGH)
                                .title("Cảnh báo nguy cơ ngập lụt mức CAO")
                                .message(buildMessage(
                                                prediction.getArea().getTenkhuvuc(),
                                                predictionDate))
                                .status(AlertStatus.PENDING)
                                .createdAt(LocalDateTime.now())
                                .build();
        }

        private String buildMessage(
                        String areaName,
                        Date predictionDate) {

                String formattedDate = new SimpleDateFormat("dd/MM/yyyy")
                                .format(predictionDate);

                return String.format(
                                """
                                                AI dự báo khu vực %s có nguy cơ ngập lụt mức CAO vào ngày %s.

                                                Đề nghị Đội trưởng chủ động theo dõi tình hình, chuẩn bị lực lượng và phương tiện sẵn sàng ứng phó khi cần thiết.
                                                """,
                                areaName,
                                formattedDate);
        }
}