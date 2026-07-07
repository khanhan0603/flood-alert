package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.flood_alert.enums.StatusSOS;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

// Người dân xem chi tiết SOS
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CitizenSosDetailResponse {

    UUID id;

    String trackingCode;

    String phoneNumber;

    Integer victimCount;

    Boolean injured;

    Boolean trapped;

    Boolean vulnerable;

    String description;

    BigDecimal lat;

    BigDecimal lon;

    String address;

    StatusSOS status;

    LocalDateTime createdAt;

    List<CitizenAssignmentResponse> assignments;
}