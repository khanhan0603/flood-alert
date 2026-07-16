package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "iot_area_aggregates", uniqueConstraints = @UniqueConstraint(name = "uk_area_recorded_at", columnNames = {
                "area_id", "recorded_at" }), indexes = {
                                @Index(name = "idx_area_recorded_at", columnList = "area_id, recorded_at")
                })
public class IoTAreaAggregates extends BaseEntity {
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "area_id")
        Area area;

        @Column(precision = 10, scale = 2)
        BigDecimal avgWater;

        @Column(precision = 10, scale = 2)
        BigDecimal minWater;

        @Column(precision = 10, scale = 2)
        BigDecimal maxWater;

        @Column(precision = 10, scale = 2)
        BigDecimal currentWater;

        @Column(name = "total_device_count")
        Integer totalDeviceCount;

        @Column(precision = 10, scale = 2)
        BigDecimal waterRiseRatePerMinute;

        @Column(name = "danger_ratio")
        Double dangerRatio;

        @Column(name = "danger_duration_minutes")
        Integer dangerDurationMinutes;

        @Column(name = "recorded_at")
        LocalDateTime recordedAt;

}
