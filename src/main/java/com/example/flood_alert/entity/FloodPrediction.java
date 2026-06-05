package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.RiskLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "flood_predictions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloodPrediction extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "madulieu", nullable = false)
    WeatherData weatherData;

    @ManyToOne
    @JoinColumn(name = "sensor_reading_id")
    IoTSensorReading sensorReading;

    @Column(name = "lead1_probability")
    Double lead1Probability;

    @Enumerated(EnumType.STRING)
    RiskLevel lead1;

    @Column(name = "lead2_probability")
    Double lead2Probability;

    @Enumerated(EnumType.STRING)
    RiskLevel lead2;

    @Column(name = "lead3_probability")
    Double lead3Probability;

    @Enumerated(EnumType.STRING)
    RiskLevel lead3;

    LocalDateTime predictedAt;

    @ManyToOne
    @JoinColumn(name = "area_id")
    Area area;
}
