package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.dbo.response.FloodPredictionResponse;
import com.example.flood_alert.entity.FloodPrediction;

import jakarta.transaction.Transactional;

public interface PredictionRepository extends JpaRepository<FloodPrediction, UUID> {

    @Query(value = """
                SELECT fp.*
                FROM flood_predictions fp
                INNER JOIN (
                    SELECT area_id, MAX(predicted_at) AS max_predicted_at
                    FROM flood_predictions
                    GROUP BY area_id
                ) latest
                    ON fp.area_id = latest.area_id
                   AND fp.predicted_at = latest.max_predicted_at
            """, nativeQuery = true)
    List<FloodPrediction> findLatestPredictionsForAllAreas();

    @Query("""
                SELECT new com.example.flood_alert.dbo.response.FloodPredictionResponse(
                    fp.lead1,
                    fp.lead1Probability,
                    fp.lead2,
                    fp.lead2Probability,
                    fp.lead3,
                    fp.lead3Probability,
                    fp.predictedAt,
                    fp.weatherFrom,
                    fp.weatherTo,
                    a.tenkhuvuc
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