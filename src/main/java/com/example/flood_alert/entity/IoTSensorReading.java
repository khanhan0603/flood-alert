package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.WaterStatus;

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
@Table(name = "iot_sensor_readings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IoTSensorReading extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    IoTDevice device;

    @Column(nullable = false)
    Double waterLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    WaterStatus status;

    @Column(nullable = false, name="is_valid")
    Boolean valid;

    @Column(nullable = false)
    LocalDateTime recordedAt;
}