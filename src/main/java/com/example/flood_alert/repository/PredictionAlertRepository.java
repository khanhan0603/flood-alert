package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.PredictionAlert;
import com.example.flood_alert.enums.AlertStatus;

public interface PredictionAlertRepository extends JpaRepository<PredictionAlert, UUID> {

    List<PredictionAlert> findByStatus(AlertStatus status);

}