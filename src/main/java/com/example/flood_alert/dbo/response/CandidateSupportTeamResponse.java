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

// Province xem danh sách Team phù hợp để điều phối theo từng Support Item
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateSupportTeamResponse {

    UUID teamId;

    String teamName;

    String leaderName;

    String leaderPhone;

    BigDecimal distanceKm;

    Long availableGroupCount;
}