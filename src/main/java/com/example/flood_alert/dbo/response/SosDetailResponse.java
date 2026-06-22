package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.StatusSOS;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Team leader xem chi tiet SOS
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SosDetailResponse {
    UUID id;

    UUID teamId;

    String teamName;

    String phoneNumber;

    Integer victimCount;

    Boolean injured;

    Boolean trapped;

    Boolean vulnerable;

    String description;

    Priority priority;

    Integer baseSeverityScore;

    String priorityReason;

    EnvironmentRisk environmentRisk;

    BigDecimal lat;

    BigDecimal lon;

    String address;

    StatusSOS status;

    LocalDateTime createdAt;

    List<SosAssignmentResponse> assignments;

    List<SupportRequestResponse> supportRequests;
}
