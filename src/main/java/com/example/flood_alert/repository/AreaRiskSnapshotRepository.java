package com.example.flood_alert.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.AreaRiskSnapshot;

import io.lettuce.core.dynamic.annotation.Param;

public interface AreaRiskSnapshotRepository
                extends JpaRepository<AreaRiskSnapshot, UUID> {

        @Query("""
                            SELECT ars
                            FROM AreaRiskSnapshot ars
                            WHERE ars.area.id = :areaId
                            ORDER BY ars.snapshotAt DESC
                        """)
        Page<AreaRiskSnapshot> findByAreaId(
                        @Param("areaId") UUID areaId,
                        Pageable pageable);

        Optional<AreaRiskSnapshot> findTopByAreaIdOrderBySnapshotAtDesc(
                        UUID areaId);

        @Query("""
            SELECT as
            FROM AreaRiskSnapshot as
            JOIN FETCH as.area
            WHERE as.area.id = :areaId
                AND as.snapshotAt = (
                    SELECT MAX(sub.snapshotAt)
                    FROM AreaRiskSnapshot sub
                    WHERE sub.area.id = as.area.id
                )
        """)
        Optional<AreaRiskSnapshot> findLatestSnapshotByAreaId(@Param("areaId") UUID areaId);
}