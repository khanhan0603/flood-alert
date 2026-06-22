package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.StatusSOS;

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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "sos_requests", indexes = {
        @Index(name = "idx_sos_status_priority", columnList = "status,priority"),
        @Index(name = "idx_sos_area_status", columnList = "area_id,status"),
        @Index(name = "idx_sos_location", columnList = "lat,lon")
})
public class SosRequest extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "area_id")
    Area area;

    @Column(nullable = false)
    String sodt;

    String clientDeviceId;

    String ipAddress;

    // Người dân chưa đăng nhập
    @Column(nullable = false)
    Boolean anonymous;

    @Column(nullable = false)
    Integer victimCount;

    @Column(nullable = false)
    BigDecimal lat;

    @Column(nullable = false)
    BigDecimal lon;

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

    // Mức độ khẩn cấp cuối cùng của SOS
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Priority priority;

    // Điểm nghiêm trọng gốc tại thời điểm tạo SOS
    // Được tính từ
    // injured
    // trapped
    // vulnerable
    // victimCount
    // environmentRisk
    // Ví dụ
    // 1 người bị thương
    // 1 người mắc kẹt
    // EnvironmentRisk=HIGH
    @Column(nullable = false)
    Integer baseSeverityScore;

    // Giải thích tại sao hệ thống gán Priority này
    @Column(nullable = false, length = 500)
    String priorityReason;

    // Mức độ rủi ro trong môi trường
    // Được tính từ:
    // waterRiseRate
    // dangerRatio
    // dangerDuration
    // predictionRiskLevel
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EnvironmentRisk environmentRisk;

    // Snapshot môi trường
    // Snapshot tốc độ tăng/giảm mực nước lúc SOS đc tạo
    // Ví dụ:
    // 11.5
    // nghĩa là:
    // 11.5 cm/phút
    Double snapshotWaterRise;

    //Snapshot tỉ lệ cảm biến vượt ngưỡng
    Double snapshotDangerRatio;

    //Snapshot dự báo lũ của AI theo thời tiết
    Double snapshotPredictionProbability;

    // Thời điểm cập nhật vị trí gần nhất
    // Lần tạo SOS đầu tiên sẽ bằng createdAt
    LocalDateTime lastLocationUpdate;

    //Vị trí hiện tại có đáng tin ko, lỡ người dân nhập tay sai vị trí
    @Column(nullable = false)
    Boolean locationConfirmed;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StatusSOS status = StatusSOS.PENDING;

    @CreationTimestamp
    @Column(nullable = false,updatable = false, name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    RescueTeam team;
}