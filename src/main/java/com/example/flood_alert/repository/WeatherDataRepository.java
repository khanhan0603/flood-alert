package com.example.flood_alert.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.WeatherData;

public interface WeatherDataRepository extends JpaRepository<WeatherData,UUID>{
    boolean existsByAreaAndTime(Area area, LocalDateTime time);
}
