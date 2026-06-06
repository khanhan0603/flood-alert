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
                WHERE d.trangThai = :trangThai
            """)
    List<IoTDevice> findByTrangThai(@Param("trangThai") DeviceStatus trangThai);
}
