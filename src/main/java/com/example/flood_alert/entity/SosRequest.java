package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.StatusSOS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "sos_requests", indexes = {
        @Index(name = "idx_sos_status_priority", columnList = "status,priority"),
        @Index(name = "idx_sos_area_status", columnList = "area_id,status")
})
public class SosRequest extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "area_id")
    Area area;

    String sodt;

    String clientDeviceId;

    String ipAddress;

    @Column(nullable = false)
    Integer victimCount;

    @Column(nullable = false)
    Double lat;

    @Column(nullable = false)
    Double lon;

    String diachi;

    Double accuracy;

    @Column(nullable = false)
    Boolean injured;

    @Column(nullable = false)
    Boolean trapped;

    @Column(nullable = false)
    Boolean vulnerable;

    @Column(columnDefinition = "TEXT")
    String mota;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StatusSOS status;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    LocalDateTime updatedAt;
}