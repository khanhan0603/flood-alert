package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.DeviceAlert;

public interface DeviceAlertRepository extends JpaRepository<DeviceAlert, UUID> {
    
}
