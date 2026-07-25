package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.RiskLevel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FloodAlertPopupResponse {

    UUID id;

    String title;

    String message;

    String tenkhuvuc;

    RiskLevel riskLevel;

    LocalDateTime createdAt;
}
