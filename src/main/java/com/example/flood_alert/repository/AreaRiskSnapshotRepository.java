package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.AreaRiskSnapshot;

public interface AreaRiskSnapshotRepository
        extends JpaRepository<AreaRiskSnapshot, UUID> {

    Optional<AreaRiskSnapshot> findTopByAreaIdOrderBySnapshotTimeDesc(UUID areaId);

    Page<AreaRiskSnapshot> findByAreaIdOrderBySnapshotTimeDesc(
            UUID areaId,
            Pageable pageable);
}