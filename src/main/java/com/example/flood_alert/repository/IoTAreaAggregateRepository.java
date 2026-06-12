package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.entity.IoTAreaAggregates;

public interface IoTAreaAggregateRepository extends JpaRepository<IoTAreaAggregates, UUID> {
    Optional<IoTAreaAggregates> findTopByAreaIdOrderByRecordedAtDesc(UUID areaId);

    @Transactional(readOnly = true)
    @Query("""
                SELECT ia
                FROM IoTAreaAggregates ia
                JOIN FETCH ia.area
                WHERE ia.recordedAt = (
                    SELECT MAX(sub.recordedAt)
                    FROM IoTAreaAggregates sub
                    WHERE sub.area.id = ia.area.id
                )
            """)
    List<IoTAreaAggregates> findLatestAggregateOfEachArea();

    @Query("""
                SELECT ia
                FROM IoTAreaAggregates ia
                WHERE ia.area.id = :areaId
                ORDER BY ia.recordedAt DESC
            """)
    @EntityGraph(attributePaths = { "area" })
    Page<IoTAreaAggregates> findByAreaId(
            @Param("areaId") UUID areaId,
            Pageable pageable);
}
