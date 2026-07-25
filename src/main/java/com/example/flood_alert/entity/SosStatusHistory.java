package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "sos_status_history", indexes = {
        @Index(name = "idx_sos_status_history_sos", columnList = "sos_id,created_at")
})
public class SosStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_id", nullable = false)
    SosRequest sos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StatusSOS status;

    @Column(length = 255)
    String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "created_at")
    LocalDateTime createdAt;
}