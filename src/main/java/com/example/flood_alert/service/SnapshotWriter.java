package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.FloodPrediction;
import com.example.flood_alert.entity.IoTAreaAggregates;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.event.SnapshotCreatedEvent;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.IoTAreaAggregateRepository;
import com.example.flood_alert.repository.PredictionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SnapshotWriter {

        AreaRiskSnapshotRepository areaRiskSnapshotRepository;
        IoTAreaAggregateRepository ioTAreaAggregateRepository;
        PredictionRepository floodPredictionRepository;
        AreaRepository areaRepository;
        RiskScoreCalculator riskScoreCalculator;
        ApplicationEventPublisher eventPublisher;
        
        @Transactional
        public void generateSnapshot(UUID areaId) {
                List<IoTAreaAggregates> aggregates = ioTAreaAggregateRepository
                                .findRecentAggregates(areaId, PageRequest.of(0, 15));

                if (aggregates.size() < 2) {
                        log.info("Skip snapshot area={} — only {} aggregates", areaId, aggregates.size());
                        return;
                }

                FloodPrediction prediction = floodPredictionRepository
                                .findTopByAreaIdOrderByPredictedAtDesc(areaId)
                                .orElse(null);

                RiskLevel riskLevel = riskScoreCalculator.calculate(aggregates, prediction);

                double iotRiskScore = riskScoreCalculator.calculateScore(aggregates);
                int dangerAggregateCount = (int) riskScoreCalculator.countDangerAggregates(aggregates);
                double dangerPercent = ((double) dangerAggregateCount / aggregates.size()) * 100;

                IoTAreaAggregates latestAggregate = aggregates.get(0);

                AreaRiskSnapshot snapshot = AreaRiskSnapshot.builder()
                                .area(areaRepository.getReferenceById(areaId))
                                .prediction(prediction)
                                .riskLevel(riskLevel)
                                .iotRiskScore(iotRiskScore)
                                .dangerRatio(latestAggregate.getDangerRatio())
                                .dangerDurationMinutes(latestAggregate.getDangerDurationMinutes())
                                .waterRiseRatePerMinute(latestAggregate.getWaterRiseRatePerMinute())
                                .predictionRiskLevel(prediction != null ? prediction.getLead1() : null)
                                .predictionProbability(prediction != null ? prediction.getLead1Probability() : null)
                                .dangerAggregateCount(dangerAggregateCount)
                                .dangerPercent(dangerPercent)
                                .snapshotAt(LocalDateTime.now())
                                .createdAt(LocalDateTime.now())
                                .build();

                AreaRiskSnapshot savedSnapshot = areaRiskSnapshotRepository.save(snapshot);
                log.info("Snapshot saved. Publish event");
                eventPublisher.publishEvent(new SnapshotCreatedEvent(savedSnapshot.getId()));
        }
}