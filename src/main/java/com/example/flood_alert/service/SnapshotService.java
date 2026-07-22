package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.flood_alert.dbo.response.AreaDataByParentResponse;
import com.example.flood_alert.dbo.response.AreaRiskSnapshotResponse;
import com.example.flood_alert.dbo.response.RegionalForecastResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.FloodPrediction;
import com.example.flood_alert.entity.IoTAreaAggregates;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
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
public class SnapshotService {
        AreaRiskSnapshotRepository areaRiskSnapshotRepository;
        IoTAreaAggregateRepository ioTAreaAggregateRepository;
        PredictionRepository floodPredictionRepository;
        AreaRepository areaRepository;
        RiskScoreCalculator riskScoreCalculator;
        AlertService alertService;

        @Transactional
        public void generateSnapshot(UUID areaId) {
                // Lấy 15 aggregate gần nhất (~15 phút) thay vì 2
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
                log.info("Snapshot saved. Call AlertService");

                TransactionSynchronizationManager.registerSynchronization(
                                new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                                try {
                                                        alertService.processSnapshot(savedSnapshot);
                                                        log.info("AlertService finished");
                                                } catch (Exception e) {
                                                        log.error("Process snapshot failed", e);
                                                }
                                        }
                                });
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
                log.info("Rollback only = {}",TransactionAspectSupport.currentTransactionStatus().isRollbackOnly());


                log.info("FINISH GENERATE SNAPSHOTS");
        }

        public AreaRiskSnapshot getAreaRiskSnapshots(UUID areaId) {
                AreaRiskSnapshot areaRiskSnapshot = areaRiskSnapshotRepository.findLatestSnapshotByAreaId(areaId)
                                .orElseThrow(() -> new AppException(ErrorCode.SNAPSHOT_NOT_FOUND));

                return areaRiskSnapshot;
        }

        public Page<AreaRiskSnapshotResponse> getListSnapshotByAreaId(UUID areaId, LocalDateTime snapBegin,
                        LocalDateTime snapEnd, Pageable pageable) {
                Page<AreaRiskSnapshot> areaRiskSnapshots = areaRiskSnapshotRepository
                                .findLatestSnapshotsByAreaIdBySnapshotAt(areaId, snapBegin, snapEnd, pageable);
                if (areaRiskSnapshots.isEmpty()) {
                        throw new AppException(ErrorCode.SNAPSHOT_NOT_FOUND);
                }
                return areaRiskSnapshots.map(areaSnapshot -> AreaRiskSnapshotResponse.builder()
                                .areaId(areaSnapshot.getArea().getId())
                                .tenkhuvuc(areaSnapshot.getArea().getTenkhuvuc())
                                .riskLevel(areaSnapshot.getRiskLevel())
                                .iotRiskScore(areaSnapshot.getIotRiskScore())
                                .predictionProbability(areaSnapshot.getPredictionProbability())
                                .dangerRatio(areaSnapshot.getDangerRatio())
                                .dangerDurationMinutes(areaSnapshot.getDangerDurationMinutes())
                                .waterRiseRatePerMinute(areaSnapshot.getWaterRiseRatePerMinute())
                                .dangerAggregateCount(areaSnapshot.getDangerAggregateCount())
                                .dangerPercent(areaSnapshot.getDangerPercent())
                                .predictionRiskLevel(areaSnapshot.getPredictionRiskLevel())
                                .snapshotAt(areaSnapshot.getSnapshotAt())
                                .createdAt(areaSnapshot.getCreatedAt())
                                .build());
        }

        @Transactional
        public List<RegionalForecastResponse> getRegionalForecast(UUID areaId) {

                Area area = areaRepository.findById(areaId)
                                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

                if (area.getParent() == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                List<AreaDataByParentResponse> areas = areaRepository
                                .findByParentId(area.getParent().getId());

                List<UUID> areaIds = areas.stream()
                                .map(AreaDataByParentResponse::getId)
                                .toList();

                Map<UUID, AreaRiskSnapshot> latestSnapshots = areaRiskSnapshotRepository
                                .findLatestSnapshotsByAreaIds(areaIds)
                                .stream()
                                .collect(Collectors.toMap(
                                                snapshot -> snapshot.getArea().getId(),
                                                snapshot -> snapshot));

                return areas.stream()
                                .map(areaItem -> {

                                        AreaRiskSnapshot snapshot = latestSnapshots.get(areaItem.getId());

                                        if (snapshot == null) {
                                                return null;
                                        }

                                        return RegionalForecastResponse.builder()
                                                        .areaId(areaItem.getId())
                                                        .tenkhuvuc(areaItem.getTenkhuvuc())
                                                        .riskLevel(snapshot.getRiskLevel())
                                                        .predictionProbability(snapshot.getPredictionProbability())
                                                        .predictionRiskLevel(snapshot.getPredictionRiskLevel())
                                                        .snapshotAt(snapshot.getSnapshotAt())
                                                        .build();
                                })
                                .filter(Objects::nonNull)
                                .toList();
        }
}
