package com.example.flood_alert.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.example.flood_alert.enums.SupportRequestSource;
import com.example.flood_alert.enums.SupportRequestStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "support_requests", indexes = {

        @Index(name = "idx_support_status", columnList = "status"),
        @Index(name = "idx_support_requested_by", columnList = "requested_by"),
        @Index(name = "idx_support_sos", columnList = "sos_id")
})
public class SupportRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_id", nullable = false)
    SosRequest sos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SupportRequestStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "supportRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    List<SupportRequestItem> items = new ArrayList<>();

    @Column(nullable = false, length = 500)
    String reason;

    @Column(columnDefinition = "TEXT")
    String provinceResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    User approvedBy;

    LocalDateTime reviewedAt;
    @CreationTimestamp
    LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SupportRequestSource source;
}
