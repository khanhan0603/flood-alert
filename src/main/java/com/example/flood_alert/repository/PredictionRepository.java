package com.example.flood_alert.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.dbo.response.FloodPredictionResponse;
import com.example.flood_alert.dbo.response.HighRiskAreaResponse;
import com.example.flood_alert.entity.FloodPrediction;
import com.example.flood_alert.enums.RiskLevel;

import jakarta.transaction.Transactional;

public interface PredictionRepository extends JpaRepository<FloodPrediction, UUID> {
    @Query("""
                SELECT new com.example.flood_alert.dbo.response.FloodPredictionResponse(
                    a.id,
                    a.tenkhuvuc,
                    fp.lead1,
                    fp.lead1Probability,
                    fp.lead2,
                    fp.lead2Probability,
                    fp.lead3,
                    fp.lead3Probability,
                    fp.predictedAt,
                    fp.weatherFrom,
                    fp.weatherTo
                )
                FROM FloodPrediction fp
                JOIN fp.area a
                WHERE fp.predictedAt = (
                    SELECT MAX(fp2.predictedAt)
                    FROM FloodPrediction fp2
                    WHERE fp2.area.id = fp.area.id
                )
            """)
    List<FloodPredictionResponse> findLatestPredictionsForAllAreas();

    Optional<FloodPrediction> findTopByAreaIdOrderByPredictedAtDesc(UUID areaId);

    @Query("""
                SELECT new com.example.flood_alert.dbo.response.FloodPredictionResponse(
                    a.id,
                    a.tenkhuvuc,
                    fp.lead1,
                    fp.lead1Probability,
                    fp.lead2,
                    fp.lead2Probability,
                    fp.lead3,
                    fp.lead3Probability,
                    fp.predictedAt,
                    fp.weatherFrom,
                    fp.weatherTo
                )
                FROM FloodPrediction fp
                JOIN fp.area a
                WHERE a.id = :areaId
            """)
    List<FloodPredictionResponse> findPredictionByArea(@Param("areaId") UUID areaId);

    @Modifying
    @Transactional
    @Query(value = """
                DELETE FROM flood_predictions
                WHERE predicted_at < NOW() - INTERVAL '90 days'
            """, nativeQuery = true)
    int deleteOldPredictions();

    /**
     * Liên kết các bản ghi FloodPrediction của một phiên chạy
     * với PredictionJobHistory vừa được tạo.
     *
     * Quy ước:
     * - Ca sáng: 06:30 - trước 18:30.
     * - Ca tối: 18:30 - trước 06:30 hôm sau.
     *
     * predicted_at được lưu theo UTC nên cần cộng thêm 7 giờ
     * trước khi xác định ngày và ca chạy.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE flood_predictions fp
            SET prediction_job_history_id = :historyId
            WHERE fp.prediction_job_history_id IS NULL
              AND DATE(fp.predicted_at + INTERVAL '7 hour') = DATE(:startedAt)
              AND (
                    (
                        :jobType = 'MORNING'
                        AND (fp.predicted_at + INTERVAL '7 hour')::time >= TIME '06:30:00'
                        AND (fp.predicted_at + INTERVAL '7 hour')::time < TIME '18:30:00'
                    )
                    OR
                    (
                        :jobType = 'EVENING'
                        AND (
                            (fp.predicted_at + INTERVAL '7 hour')::time >= TIME '18:30:00'
                            OR
                            (fp.predicted_at + INTERVAL '7 hour')::time < TIME '06:30:00'
                        )
                    )
              )
            """, nativeQuery = true)
    int linkPredictionJobHistory(
            @Param("historyId") UUID historyId,
            @Param("startedAt") LocalDateTime startedAt,
            @Param("jobType") String jobType);

    // THỐNG KÊ

    /**
     * Đếm tổng số khu vực đã được dự báo
     * trong một phiên chạy AI.
     */
    long countByPredictionJobHistoryId(UUID predictionJobHistoryId);

    /**
     * Đếm số khu vực có mức nguy cơ LOW/MEDIUM/HIGH
     * theo Lead1 của một phiên chạy AI.
     */
    long countByPredictionJobHistoryIdAndLead1(
            UUID predictionJobHistoryId,
            RiskLevel riskLevel);

    /**
     * Lấy Top 10 khu vực có xác suất dự báo
     * lũ cao nhất theo Lead1.
     * Chỉ có HIGH
     */
    @Query("""
                SELECT new com.example.flood_alert.dbo.response.HighRiskAreaResponse(
                    fp.area.tenkhuvuc,
                    fp.lead1Probability
                )
                FROM FloodPrediction fp
                WHERE fp.predictionJobHistory.id = :predictionJobHistoryId
                  AND fp.lead1 = com.example.flood_alert.enums.RiskLevel.HIGH
                ORDER BY fp.lead1Probability DESC
            """)
    List<HighRiskAreaResponse> findTopHighRiskAreas(
            UUID predictionJobHistoryId,
            Pageable pageable);
}