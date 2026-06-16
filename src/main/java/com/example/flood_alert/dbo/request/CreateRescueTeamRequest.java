package com.example.flood_alert.dbo.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRescueTeamRequest {
    @NotBlank
    String name;

    String description;

    @NotNull
    UUID areaId;
}
