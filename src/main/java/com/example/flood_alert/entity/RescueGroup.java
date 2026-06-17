package com.example.flood_alert.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.RescueGroupStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rescue_groups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescueGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    RescueTeam team;

    @Column(nullable = false)
    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    User leader;

    @Enumerated(EnumType.STRING)
    RescueGroupStatus status;

    Double currentLat;

    Double currentLon;

    boolean hasBoat;

    boolean hasMedical;

    String notes;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
