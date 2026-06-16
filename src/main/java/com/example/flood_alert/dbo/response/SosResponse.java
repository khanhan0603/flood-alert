package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.Priority;
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

    String id;

    StatusSOS status;

    Priority priority;

    LocalDateTime createdAt;
}