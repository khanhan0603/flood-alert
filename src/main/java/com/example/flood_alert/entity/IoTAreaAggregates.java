package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.WaterStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "iot_area_aggregates", indexes = {
        @Index(name = "idx_area_recorded_at", columnList = "area_id, recorded_at")
})
public class IoTAreaAggregates extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    Area area;

    @Column(name = "avg_water")
    Double avgWater;

    @Column(name = "max_water")
    Double maxWater;

    @Column(name = "total_device_count")
    Integer totalDeviceCount;

    @Column(name = "danger_device_count")
    Integer dangerDeviceCount;

    @Column(name = "safe_device_count")
    Integer safeDeviceCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "iot_status", nullable = false)
    WaterStatus iotStatus;

    @Column(nullable = false)
    Double dangerRatio;

    @Column(name = "recorded_at")
    LocalDateTime recordedAt;

}
