package com.example.flood_alert.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flood_alert.entity.PredictionJobHistory;

public interface PredictionJobHistoryRepository extends JpaRepository<PredictionJobHistory, UUID> {
    // Danh sách các lịch sử chạy AI theo thời gian tạo giảm dần
    Page<PredictionJobHistory> findAllByOrderByStartedAtDesc(Pageable pageable);

    // THỐNG KÊ
    /**
     * Lấy phiên dự báo AI mới nhất.
     */
    Optional<PredictionJobHistory> findFirstByOrderByStartedAtDesc();

    /**
     * Tìm phiên dự báo AI theo ngày và ca chạy.
     */
    @Query(value = """
            SELECT *
            FROM prediction_job_history
            WHERE DATE(started_at) = :date
              AND job_type = :jobType
            LIMIT 1
            """, nativeQuery = true)
    Optional<PredictionJobHistory> findByJobDateAndType(
            @Param("date") LocalDate date,
            @Param("jobType") String jobType);

}