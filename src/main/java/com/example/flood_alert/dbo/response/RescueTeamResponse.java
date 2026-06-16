package com.example.flood_alert.dbo.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RescueTeamResponse {
    UUID id;

    String name;

    String description;

    UUID areaId;

    String areaName;

    UUID leaderId;

    String leaderName;
}
