package com.example.flood_alert.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.IoTDevice;


public interface IoTDeviceRepository extends JpaRepository<IoTDevice,UUID>{
    Optional<IoTDevice> findByDeviceCode(String deviceCode);
}
