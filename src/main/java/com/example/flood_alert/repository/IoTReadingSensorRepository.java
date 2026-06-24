package com.example.flood_alert.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.IoTSensorReading;

import io.lettuce.core.dynamic.annotation.Param;

public interface IoTReadingSensorRepository extends JpaRepository<IoTSensorReading, UUID> {
    @Query("""
                SELECT r
                FROM IoTSensorReading r
                WHERE r.recordedAt IN (
                    SELECT MAX(r2.recordedAt)
                    FROM IoTSensorReading r2
                    WHERE r2.device.id = r.device.id
                )
                AND r.device.area.id = :areaId
                AND r.device.trangThai = com.example.flood_alert.enums.DeviceStatus.ACTIVE
            """)
    List<IoTSensorReading> findLatestReadingsByAreaId(@Param("areaId") UUID areaId);

    @Query("""
                SELECT r
                FROM IoTSensorReading r
                WHERE r.device.area.id = :areaId
                    AND r.device.trangThai = com.example.flood_alert.enums.DeviceStatus.ACTIVE
                    AND r.recordedAt >= :startTime
                    AND r.recordedAt < :endTime
                    AND r.valid = true
            """)
    List<IoTSensorReading> findByAreaIdAndTimeRange(@Param("areaId") UUID areaId, LocalDateTime startTime,
            LocalDateTime endTime);

    // Lấy danh sách mực nước theo device
    List<IoTSensorReading> findByDeviceIdOrderByRecordedAtDesc(
            UUID deviceId,
            Pageable pageable);
}
