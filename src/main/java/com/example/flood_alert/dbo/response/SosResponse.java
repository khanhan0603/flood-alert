package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.DispatcherType;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.SosSource;
import com.example.flood_alert.enums.StatusSOS;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SosResponse {

    UUID id;

    Boolean alreadyExists;

    Priority priority;

    StatusSOS status;

    EnvironmentRisk environmentRisk;

    Integer victimCount;

    String priorityReason;

    String mota;

    SosSource sosSource;

    UUID callEventId;
    
    String trackingCode;

    // Người đang điều phối
    UUID dispatcherUserId;

    String dispatcherName;

    DispatcherType dispatcherType;

    LocalDateTime createdAt;
}