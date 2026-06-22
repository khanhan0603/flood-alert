package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.StatusSOS;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Team leader xem SOS
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SosSummaryResponse {
    UUID id;

    String phoneNumber;

    Integer victimCount;

    Boolean injured;

    Boolean trapped;

    Boolean vulnerable;

    Priority priority;

    EnvironmentRisk environmentRisk;

    StatusSOS status;

    BigDecimal lat;

    BigDecimal lon;

    String address;

    LocalDateTime createdAt;
}
