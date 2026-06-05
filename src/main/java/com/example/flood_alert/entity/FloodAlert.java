package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.enums.StatusAlert;

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
@Table(name = "flood_alerts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloodAlert extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "sensor_reading_id")
     IoTSensorReading sensorReading;

    @ManyToOne
    @JoinColumn(name = "madudoan")
     FloodPrediction prediction;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
     User user;

    @ManyToOne
    @JoinColumn(name = "area_id", nullable = false)
     Area area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
     RiskLevel riskLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
     String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
     Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
     StatusAlert status;

     LocalDateTime sentAt;

    @Column(nullable = false)
     LocalDateTime createdAt;
}
