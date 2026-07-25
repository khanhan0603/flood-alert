package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.SosStatusHistory;

public interface SosStatusHistoryRepository extends JpaRepository<SosStatusHistory, UUID> {
    List<SosStatusHistory> findBySosIdOrderByCreatedAtAsc(UUID sosId);
}