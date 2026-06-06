package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.flood_alert.enums.DeviceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "iot_devices")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDevice extends BaseEntity {
    @Column(name="device_code")
    private String deviceCode;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @Column(name = "ten_thietbi")
    private String tenThietBi;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lon;

    @Column(name = "nguong_canh_bao")
    private Double nguongCanhBao;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private DeviceStatus trangThai;

    private LocalDateTime lastSeenAt;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}