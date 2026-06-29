package com.example.flood_alert.dbo.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

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
public class AuthenticateResponse {
    String accessToken;
    String refreshToken;
    boolean authenticated;

    UUID id;
    UUID areaId;
    String hoten;
    String sodt;
    String role;
    UUID teamId;
    String teamName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean isTeamLeader;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean isGroupLeader;
}
