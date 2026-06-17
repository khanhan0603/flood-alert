package com.example.flood_alert.dbo.response;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamLeaderItemResponse {
    UUID teamId;

    String teamName;

    UUID leaderId;

    String leaderName;

    String phone;
}
