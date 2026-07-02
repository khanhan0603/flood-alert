package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.flood_alert.enums.SupportRequestItemStatus;
import com.example.flood_alert.enums.SupportType;

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
@Table(name = "support_request_items", indexes = {
        @Index(name = "idx_support_item_request", columnList = "support_request_id"),
        @Index(name = "idx_support_item_type", columnList = "support_type")
})
@Entity
public class SupportRequestItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_request_id", nullable = false)
    SupportRequest supportRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SupportType supportType;

    @Column(nullable = false)
    Integer requiredGroupCount;

    // Đội cứu hộ đc chỉ định cho chi tiết hỗ trợ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_team_id")
    RescueTeam assignedTeam;

    // Trạng thái từng item
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SupportRequestItemStatus status;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    // Phản hồi của tỉnh
    @Column(columnDefinition = "TEXT")
    String provinceNote;

    // Phản hồi của đội được chỉ định
    @Column(columnDefinition = "TEXT")
    String teamResponse;

    // Số group đã được Team Leader phân công
    @Column(nullable = false)
    @Builder.Default
    Integer assignedGroupCount = 0;

    //Số group hoàn thành
    @Column(nullable = false)
    @Builder.Default
    Integer completedGroupCount = 0;
}
