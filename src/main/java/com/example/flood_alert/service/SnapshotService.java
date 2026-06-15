package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.FloodPrediction;
import com.example.flood_alert.entity.IoTAreaAggregates;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.IoTAreaAggregateRepository;
import com.example.flood_alert.repository.PredictionRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SnapshotService {
    AreaRiskSnapshotRepository areaRiskSnapshotRepository;
    IoTAreaAggregateRepository ioTAreaAggregateRepository;
    PredictionRepository floodPredictionRepository;
    AreaRepository areaRepository;
    RiskScoreCalculator riskScoreCalculator;

    @Transactional
    public void generateSnapshot(UUID areaId) {
        // Lấy 15 record gần nhất
        List<IoTAreaAggregates> aggregates = ioTAreaAggregateRepository.findRecentAggregates(areaId,
                PageRequest.of(0, 15));

        if (aggregates.size() < 15) {
            log.info(
                    "Skip snapshot area={} because only {} aggregates",
                    areaId,
                    aggregates.size());

            return;
        }
        // Lấy prediction gần nhất
        FloodPrediction prediction = floodPredictionRepository
                .findTopByAreaIdOrderByPredictedAtDesc(areaId)
                .orElse(null);

        // Đánh giá mức độ rủi ro cuối cùng
        // từ IoT + AI
        RiskLevel riskLevel = riskScoreCalculator.calculate(aggregates, prediction);

        // Điểm nguy hiểm từ IoT
        double iotRiskScore = riskScoreCalculator.calculateScore(aggregates);
        // Số Aggregate nguy hiểm (>= 0.5) trong 30 phút gần nhất
        int dangerAggregateCount = (int) riskScoreCalculator.countDangerAggregates(aggregates);
        // Tỷ lệ Aggregate nguy hiểm trên tổng số Aggregate
        double dangerPercent = (double) dangerAggregateCount / aggregates.size();

        // Lấy aggregate mới nhất của khu vực
        IoTAreaAggregates latestAggregate = aggregates.get(0);
        AreaRiskSnapshot snapshot = AreaRiskSnapshot.builder()
                .area(areaRepository.getReferenceById(areaId))
                .prediction(prediction)

                .riskLevel(riskLevel)

                .iotRiskScore(iotRiskScore)

                .dangerRatio(
                        latestAggregate.getDangerRatio())

                .dangerDurationMinutes(
                        latestAggregate.getDangerDurationMinutes())

                .waterRiseRatePerMinute(
                        latestAggregate.getWaterRiseRatePerMinute())

                .predictionRiskLevel(
                        prediction != null
                                ? prediction.getLead1()
                                : null)

                .predictionProbability(
                        prediction != null
                                ? prediction.getLead1Probability()
                                : null)

                .dangerAggregateCount(dangerAggregateCount)

                .dangerPercent(dangerPercent)

                .snapshotAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        areaRiskSnapshotRepository.save(snapshot);
    }

    @Transactional
    public void generateAllSnapshots() {

        List<UUID> areaIds = ioTAreaAggregateRepository.findAreasHasLatestAggregate();

        log.info("START GENERATE SNAPSHOTS - TOTAL AREA={}",
                areaIds.size());

        for (UUID areaId : areaIds) {

            try {

                generateSnapshot(areaId);

            } catch (Exception e) {

                log.error(
                        "Generate snapshot failed for area={}",
                        areaId,
                        e);
            }
        }

        log.info("FINISH GENERATE SNAPSHOTS");
    }
}
