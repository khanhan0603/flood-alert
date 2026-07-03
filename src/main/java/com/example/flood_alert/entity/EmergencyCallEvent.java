package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.CallEventStatus;

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
@Table(name = "emergency_call_event", indexes = {
        @Index(name = "idx_call_event_phone", columnList = "caller_phone_number"),
        @Index(name = "idx_call_event_team_status", columnList = "team_id,status")
})
public class EmergencyCallEvent extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    RescueTeam team;

    @Column(name = "caller_lat", precision = 10, scale = 7, nullable = false)
    BigDecimal callerLat;

    @Column(name = "caller_lon", precision = 10, scale = 7, nullable = false)
    BigDecimal callerLon;

    @Column(name = "caller_phone_number", nullable = false, length = 20)
    String callerPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CallEventStatus status;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_to_sos_id")
    SosRequest convertedToSos;
}
