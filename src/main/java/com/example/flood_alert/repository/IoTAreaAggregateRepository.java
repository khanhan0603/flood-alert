package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.IoTAreaAggregates;

import io.lettuce.core.dynamic.annotation.Param;

public interface IoTAreaAggregateRepository extends JpaRepository<IoTAreaAggregates, UUID> {
    Optional<IoTAreaAggregates> findTopByAreaIdOrderByRecordedAtDesc(UUID areaId);
    @Query(value = """
            SELECT DISTINCT ON (ia.area_id)
                ia.*
            FROM iot_area_aggregates ia
            ORDER BY ia.area_id, ia.recorded_at DESC
            """, nativeQuery = true)
    List<IoTAreaAggregates> findLatestAggregateOfEachArea();

    @Query("""
                SELECT ia
                FROM IoTAreaAggregates ia
                JOIN FETCH ia.area
                WHERE ia.area.id = :areaId
                ORDER BY ia.recordedAt DESC
            """)
    Page<IoTAreaAggregates> findByAreaId(
            @Param("areaId") UUID areaId,
            Pageable pageable);
}
