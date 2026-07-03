package com.example.flood_alert.repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.EmergencyCallEvent;
import com.example.flood_alert.enums.CallEventStatus;

public interface EmergencyCallEventRepository extends JpaRepository<EmergencyCallEvent, UUID> {
        /**
         * Danh sách cuộc gọi quá thời gian xử lý.
         * Dùng Scheduler/Lazy Update chuyển sang STALE.
         */
        List<EmergencyCallEvent> findByStatusAndCreatedAtBefore(
                        CallEventStatus status,
                        LocalDateTime time);

        /**
         * Lấy cuộc gọi theo Id và trạng thái.
         * Dùng khi Operator tạo SOS.
         */
        Optional<EmergencyCallEvent> findByIdAndStatus(
                        UUID id,
                        CallEventStatus status);

        /**
         * Danh sách cuộc gọi đang chờ Operator xử lý.
         * Sắp xếp theo thời gian tạo tăng dần (cuộc gọi đến trước xử lý trước).
         */
        Page<EmergencyCallEvent> findByStatusOrderByCreatedAtAsc(
                        CallEventStatus status,
                        Pageable pageable);

        /**
         * Lịch sử cuộc gọi Hotline theo trạng thái.
         */
        Page<EmergencyCallEvent> findByStatusOrderByCreatedAtDesc(
                        CallEventStatus status,
                        Pageable pageable);

}