package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.response.AiIotStatisticsResponse;
import com.example.flood_alert.dbo.response.AiPredictionStatisticsResponse;
import com.example.flood_alert.dbo.response.OverviewStatisticsResponse;
import com.example.flood_alert.entity.PredictionJobHistory;
import com.example.flood_alert.enums.PredictionJobType;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.IoTDeviceRepository;
import com.example.flood_alert.repository.PredictionJobHistoryRepository;
import com.example.flood_alert.repository.PredictionRepository;
import com.example.flood_alert.repository.RescueGroupMemberRepository;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SosRequestRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Xử lý nghiệp vụ thống kê và báo cáo cho Dashboard Admin.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsService {

    SosRequestRepository sosRequestRepository;
    RescueTeamRepository rescueTeamRepository;
    RescueGroupRepository rescueGroupRepository;
    RescueGroupMemberRepository rescueGroupMemberRepository;
    IoTDeviceRepository ioTDeviceRepository;
    PredictionRepository predictionRepository;
    PredictionJobHistoryRepository predictionJobHistoryRepository;
    AreaRiskSnapshotRepository areaRiskSnapshotRepository;

    /**
     * Lấy dữ liệu thống kê tổng quan hiển thị trên Dashboard.
     */
    public OverviewStatisticsResponse getOverview() {

        return OverviewStatisticsResponse.builder()
                // Thống kê SOS
                .totalSos(sosRequestRepository.count())
                .todaySos(sosRequestRepository.countTodaySos())
                .pendingSos(sosRequestRepository.countByStatus(StatusSOS.PENDING))
                .assignedSos(sosRequestRepository.countByStatus(StatusSOS.ASSIGNED))
                .processingSos(sosRequestRepository.countByStatus(StatusSOS.PROCESSING))
                .completedSos(sosRequestRepository.countByStatus(StatusSOS.DONE))
                .cancelledSos(sosRequestRepository.countByStatus(StatusSOS.CANCELED))

                // Thống kê lực lượng cứu hộ
                .totalTeams(rescueTeamRepository.count())
                .totalGroups(rescueGroupRepository.count())
                .totalMembers(rescueGroupMemberRepository.count())

                // Thống kê thiết bị IoT
                .totalDevices(ioTDeviceRepository.count())
                .build();
    }

    /**
     * Lấy thống kê của phiên dự báo AI mới nhất.
     */
    public AiPredictionStatisticsResponse getLatestAiPredictionStatistics() {

        PredictionJobHistory history = predictionJobHistoryRepository
                .findFirstByOrderByStartedAtDesc()
                .orElseThrow(() -> new AppException(
                        ErrorCode.PREDICTION_JOB_NOT_FOUND));

        AiPredictionStatisticsResponse response = getAiPredictionStatistics(history.getId());

        response.setJobDate(history.getStartedAt().toLocalDate());
        response.setJobType(history.getJobType());

        return response;
    }

    /**
     * Lấy thống kê dự báo AI theo ngày và ca chạy.
     */
    public AiPredictionStatisticsResponse getAiPredictionStatistics(
            LocalDate date,
            PredictionJobType jobType) {

        PredictionJobHistory history = predictionJobHistoryRepository
                .findByJobDateAndType(
                        date,
                        jobType.name())
                .orElseThrow(() -> new AppException(
                        ErrorCode.PREDICTION_JOB_NOT_FOUND));

        AiPredictionStatisticsResponse response = getAiPredictionStatistics(history.getId());

        response.setJobDate(history.getStartedAt().toLocalDate());
        response.setJobType(history.getJobType());

        return response;
    }

    /**
     * Thống kê kết quả dự báo lũ của một phiên chạy AI.
     *
     * @param predictionJobHistoryId Id phiên chạy AI.
     * @return Thống kê dự báo.
     */
    private AiPredictionStatisticsResponse getAiPredictionStatistics(UUID predictionJobHistoryId) {

        return AiPredictionStatisticsResponse.builder()

                // Tổng số khu vực được dự báo.
                .totalAreas(
                        predictionRepository.countByPredictionJobHistoryId(
                                predictionJobHistoryId))

                // Số khu vực nguy cơ thấp.
                .lowRiskAreas(
                        predictionRepository.countByPredictionJobHistoryIdAndLead1(
                                predictionJobHistoryId,
                                RiskLevel.LOW))

                // Số khu vực nguy cơ trung bình.
                .mediumRiskAreas(
                        predictionRepository.countByPredictionJobHistoryIdAndLead1(
                                predictionJobHistoryId,
                                RiskLevel.MEDIUM))

                // Số khu vực nguy cơ cao.
                .highRiskAreas(
                        predictionRepository.countByPredictionJobHistoryIdAndLead1(
                                predictionJobHistoryId,
                                RiskLevel.HIGH))
                // Top khu vực có xác suất ngập cao nhất.
                .topHighRiskAreas(
                        predictionRepository.findTopHighRiskAreas(
                                predictionJobHistoryId,
                                PageRequest.of(0, 10))
                                .stream()
                                .peek(item -> item.setProbability(
                                        toPercent(item.getProbability())))
                                .toList())

                .build();
    }

    // Làm tròn 2 số
    private Double toPercent(Double probability) {

        if (probability == null) {
            return null;
        }

        return BigDecimal.valueOf(probability * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Thống kê nguy cơ lũ hiện tại dựa trên dữ liệu AI + IoT.
     */
    public AiIotStatisticsResponse getAiIotStatistics() {

        return AiIotStatisticsResponse.builder()

                // Tổng số khu vực đã có snapshot mới nhất.
                .totalAreas(
                        areaRiskSnapshotRepository.countLatestSnapshots())

                // Số khu vực nguy cơ thấp.
                .lowRiskAreas(
                        areaRiskSnapshotRepository.countLatestSnapshotsByRiskLevel(
                                RiskLevel.LOW))

                // Số khu vực nguy cơ trung bình.
                .mediumRiskAreas(
                        areaRiskSnapshotRepository.countLatestSnapshotsByRiskLevel(
                                RiskLevel.MEDIUM))

                // Số khu vực nguy cơ cao.
                .highRiskAreas(
                        areaRiskSnapshotRepository.countLatestSnapshotsByRiskLevel(
                                RiskLevel.HIGH))

                // Top 10 khu vực nguy cơ cao nhất.
                .topHighRiskAreas(
                        areaRiskSnapshotRepository.findTopHighRiskAreas(
                                PageRequest.of(0, 10))
                                .stream()
                                .peek(item -> {

                                    // Chuyển xác suất AI từ 0-1 sang %
                                    item.setPredictionProbability(
                                            toPercent(item.getPredictionProbability()));

                                    // Chuyển danger ratio từ 0-1 sang %
                                    item.setDangerRatio(
                                            toPercent(item.getDangerRatio()));
                                })
                                .toList())

                .build();
    }
}