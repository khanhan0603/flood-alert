package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.IoTDevice;
import com.example.flood_alert.enums.DeviceStatus;

import io.lettuce.core.dynamic.annotation.Param;

public interface IoTDeviceRepository extends JpaRepository<IoTDevice, UUID> {
    Optional<IoTDevice> findByDeviceCode(String deviceCode);

    @Query("""
                SELECT d
                FROM IoTDevice d
                JOIN FETCH d.area
                ORDER BY
                    CASE
                        WHEN d.trangThai= 'PENDING' THEN 1
                        WHEN d.trangThai= 'ERROR' THEN 2
                        WHEN d.trangThai= 'INACTIVE' THEN 3
                        WHEN d.trangThai= 'ACTIVE' THEN 4
                        WHEN d.trangThai= 'REJECTED' THEN 5
                        ELSE 6
                    END
            """)
    List<IoTDevice> getListOrderByTrangThai();

    @Query("""
                SELECT d
                FROM IoTDevice d
                JOIN FETCH d.area
                WHERE d.trangThai = :trangThai
            """)
    List<IoTDevice> getListByTrangThai(@Param ("trangThai") DeviceStatus trangThai);

    @Query("""
                SELECT DISTINCT d.area.id
                FROM IoTDevice d
                WHERE d.trangThai = com.example.flood_alert.enums.DeviceStatus.ACTIVE
                  AND d.area IS NOT NULL
            """)
    List<UUID> findAreaIdsHasActiveDevice();
}
