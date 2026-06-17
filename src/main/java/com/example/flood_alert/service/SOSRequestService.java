package com.example.flood_alert.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.SosRequestRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SOSRequestService {
        AreaRepository areaRepository;

        AreaRiskSnapshotRepository areaRiskSnapshotRepository;

        SosRequestRepository sosRequestRepository;

        EnvironmentRiskEvaluator environmentRiskEvaluator;

        SosPriorityCalculator sosPriorityCalculator;

        SeverityScoreCalculator severityScoreCalculator;

        PriorityReasonGenerator priorityReasonGenerator;

        @Transactional
        public SosResponse create(CreateSosRequest request) {

                // 1. Xác định khu vực từ tọa độ
                UUID areaId = areaRepository.findAreaIdByLatLon(request.getLat(), request.getLon());

                if (areaId == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                Area area = areaRepository.findById(areaId)
                                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

                // 2. Lấy snapshot mới nhất của khu vực
                AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                                .findLatestSnapshotByAreaId(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_RISK_NOT_FOUND));

                // 3. Tính EnvironmentRisk
                EnvironmentRisk environmentRisk = environmentRiskEvaluator.evaluate(snapshot);

                // 4. Tính Priority
                Priority priority = sosPriorityCalculator.calculate(request, environmentRisk);

                // 5. Tính BaseSeverityScore
                Integer baseSeverityScore = severityScoreCalculator.calculate(request, environmentRisk);

                // 6. Sinh PriorityReason
                String priorityReason = priorityReasonGenerator.generate(
                                priority,
                                request,
                                environmentRisk);

                // 7. Tạo SOS
                SosRequest sos = SosRequest.builder()
                                .area(area)

                                .anonymous(true)

                                .sodt(request.getSodt())

                                .victimCount(request.getVictimCount())

                                .lat(request.getLat())
                                .lon(request.getLon())

                                .accuracy(request.getAccuracy())

                                .injured(request.getInjured())
                                .trapped(request.getTrapped())
                                .vulnerable(request.getVulnerable())

                                .mota(request.getMota())

                                .priority(priority)

                                .baseSeverityScore(
                                                baseSeverityScore)

                                .priorityReason(
                                                priorityReason)

                                .environmentRisk(
                                                environmentRisk)

                                .snapshotWaterRise(
                                                snapshot.getWaterRiseRatePerMinute())

                                .snapshotDangerRatio(
                                                snapshot.getDangerRatio())

                                .snapshotPredictionProbability(
                                                snapshot.getPredictionProbability())

                                .locationConfirmed(false)

                                .status(StatusSOS.PENDING)

                                .build();
                                
                // Hibernate flush xuống DB ngay lập tức và đồng bộ lại entity
                sos = sosRequestRepository.saveAndFlush(sos);

                // 8. Response
                return SosResponse.builder()
                                .id(sos.getId())

                                .priority(sos.getPriority())

                                .baseSeverityScore(
                                                sos.getBaseSeverityScore())

                                .environmentRisk(
                                                sos.getEnvironmentRisk())

                                .victimCount(sos.getVictimCount())

                                .priorityReason(
                                                sos.getPriorityReason())

                                .status(sos.getStatus())

                                .mota(sos.getMota())

                                .createdAt(sos.getCreatedAt())

                                .build();
        }
}
