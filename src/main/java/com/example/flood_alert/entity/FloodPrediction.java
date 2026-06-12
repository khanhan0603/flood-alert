package com.example.flood_alert.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.example.flood_alert.enums.RiskLevel;

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

@Entity
@Table(name = "flood_predictions",indexes = {
    @Index(name="idx_predicted_at", columnList = "predicted_at")
})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloodPrediction extends BaseEntity {
    @Column(name = "lead1_probability")
    Double lead1Probability;

    @Enumerated(EnumType.STRING)
    RiskLevel lead1;

    @Column(name = "lead1_date")
    Date lead1Date;

    @Column(name = "lead2_probability")
    Double lead2Probability;

    @Enumerated(EnumType.STRING)
    RiskLevel lead2;

    @Column(name = "lead2_date")
    Date lead2Date;

    @Column(name = "lead3_probability")
    Double lead3Probability;

    @Enumerated(EnumType.STRING)
    RiskLevel lead3;

    @Column(name = "lead3_date")
    Date lead3Date;

    @Column(name = "predicted_at")
    LocalDateTime predictedAt;

    @ManyToOne
    @JoinColumn(name = "area_id")
    Area area;

    @JoinColumn(name = "weather_from")
    LocalDateTime weatherFrom;

    @JoinColumn(name = "weather_to")
    LocalDateTime weatherTo;
}
