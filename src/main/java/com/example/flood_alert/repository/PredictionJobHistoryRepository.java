package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.PredictionJobHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PredictionJobHistoryRepository extends JpaRepository<PredictionJobHistory, UUID> {
    //Danh sách các lịch sử chạy AI theo thời gian tạo giảm dần
    Page<PredictionJobHistory> findAllByOrderByStartedAtDesc(Pageable pageable);
}