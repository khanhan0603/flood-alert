package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;

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
@Table(name = "sos_assignments", indexes = {

        @Index(name = "idx_assignment_sos", columnList = "sos_id"),

        @Index(name = "idx_assignment_group", columnList = "group_id"),

        @Index(name = "idx_assignment_status", columnList = "status"),
        @Index(name = "idx_assignment_group_status", columnList = "group_id,status"), //vì cần query group hiện đang có nhiệm vụ gì
        @Index(name = "idx_assignment_sos_status", columnList = "sos_id,status")
})
public class SosAssignment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_id", nullable = false)
    SosRequest sos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    RescueGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    User assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AssignmentRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AssignmentStatus status;

    LocalDateTime assignedAt;

    LocalDateTime acknowledgedAt;

    LocalDateTime arrivedAt;

    @CreationTimestamp
    LocalDateTime createAt;
    LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    String note;
}
