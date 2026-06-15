package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.Channel;
import com.example.flood_alert.enums.RiskLevel;
import com.example.flood_alert.enums.StatusAlert;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class FloodAlertResponse {
    String tenkhuvuc;
    RiskLevel riskLevel;
    Channel channel;
    StatusAlert status;
    LocalDateTime createdAt; 
}
