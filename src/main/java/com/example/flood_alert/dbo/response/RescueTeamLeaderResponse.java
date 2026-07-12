package com.example.flood_alert.dbo.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RescueTeamLeaderResponse {

    UUID teamId;

    UUID leaderId;

    String leaderName;

    UUID deputyLeaderId;

    String deputyLeaderName;

}
