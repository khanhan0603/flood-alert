package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.dbo.response.FloodPredictionResponse;
import com.example.flood_alert.entity.FloodPrediction;

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
}