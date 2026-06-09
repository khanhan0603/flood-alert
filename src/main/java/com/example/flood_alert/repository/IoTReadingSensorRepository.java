package com.example.flood_alert.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.IoTSensorReading;

public interface IoTReadingSensorRepository extends JpaRepository<IoTSensorReading, UUID> {
    
}
