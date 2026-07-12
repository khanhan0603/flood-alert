package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "rescue_teams")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RescueTeam extends BaseEntity {

    @Column(nullable = false)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    User leader;

    // Phó trưởng nhóm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deputy_leader_id")
    User deputyLeader;

    // Số điện thoại khẩn
    @Column(name = "emergency_phone", length = 20)
    String emergencyPhone;

    // Lat,lon -> vị trí trụ sở chính của team
    @Column(name = "caller_lat", precision = 10, scale = 7, nullable = false)
    BigDecimal lat;

    @Column(name = "caller_lon", precision = 10, scale = 7, nullable = false)
    BigDecimal lon;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}