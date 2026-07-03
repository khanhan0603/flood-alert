package com.example.flood_alert.dbo.response;
import java.math.BigDecimal;
import java.util.UUID;

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
public class RescueTeamSupportResponse {
    UUID id;

    String teamName;

    UUID areaId;

    BigDecimal lat;

    BigDecimal lon;

    String leaderName;

    String leaderPhone;

    String emergencyPhone;

    Long availableBoatGroups;

    Long availableMedicalGroups;

    Long availableSearchRescueGroups;

    Long availableLogisticsGroups;

    BigDecimal distanceKm;

     // true nếu là đội đang yêu cầu hỗ trợ
    Boolean requesterTeam;
}
