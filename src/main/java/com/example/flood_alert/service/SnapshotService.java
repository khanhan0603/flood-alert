package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.response.AreaDataByParentResponse;
import com.example.flood_alert.dbo.response.AreaRiskSnapshotResponse;
import com.example.flood_alert.dbo.response.RegionalForecastResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.PredictionJobHistory;
import com.example.flood_alert.enums.PredictionJobStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.IoTAreaAggregateRepository;
import com.example.flood_alert.repository.PredictionJobHistoryRepository;

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
        AreaRepository areaRepository;
        SnapshotWriter snapshotWriter;
        PredictionJobHistoryRepository predictionJobHistoryRepository;

        public void generateAllSnapshots() {

                List<UUID> areaIds = ioTAreaAggregateRepository.findAreasHasLatestAggregate();

                PredictionJobHistory latestJob = predictionJobHistoryRepository
                                .findTopByStatusOrderByFinishedAtDesc(PredictionJobStatus.SUCCESS)
                                .orElseThrow(() -> new AppException(ErrorCode.PREDICTION_JOB_NOT_FOUND));

                log.info("START GENERATE SNAPSHOTS - TOTAL AREA={}",
                                areaIds.size());

                for (UUID areaId : areaIds) {

                        try {

                                snapshotWriter.generateSnapshot(areaId,latestJob.getId());

                        } catch (Exception e) {

                                log.error(
                                                "Generate snapshot failed for area={}",
                                                areaId,
                                                e);
                        }
                }

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

        @Transactional(readOnly = true)
        public List<AreaRiskSnapshotResponse> getLatestSnapshots() {

                return areaRiskSnapshotRepository.findLatestSnapshots()
                                .stream()
                                .map(snapshot -> AreaRiskSnapshotResponse.builder()
                                                .areaId(snapshot.getArea().getId())
                                                .tenkhuvuc(snapshot.getArea().getTenkhuvuc())
                                                .riskLevel(snapshot.getRiskLevel())
                                                .iotRiskScore(snapshot.getIotRiskScore())
                                                .predictionProbability(snapshot.getPredictionProbability())
                                                .dangerRatio(snapshot.getDangerRatio())
                                                .dangerDurationMinutes(snapshot.getDangerDurationMinutes())
                                                .waterRiseRatePerMinute(snapshot.getWaterRiseRatePerMinute())
                                                .dangerAggregateCount(snapshot.getDangerAggregateCount())
                                                .dangerPercent(snapshot.getDangerPercent())
                                                .predictionRiskLevel(snapshot.getPredictionRiskLevel())
                                                .snapshotAt(snapshot.getSnapshotAt())
                                                .createdAt(snapshot.getCreatedAt())
                                                .build())
                                .toList();
        }
}
