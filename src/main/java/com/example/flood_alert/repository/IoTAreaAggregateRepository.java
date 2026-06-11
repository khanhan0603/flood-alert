package com.example.flood_alert.repository;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.IoTAreaAggregates;



public interface IoTAreaAggregateRepository extends JpaRepository<IoTAreaAggregates, UUID> {
    
}
