package com.example.flood_alert.dbo.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateRescueTeamRequest {
    @NotBlank
    String name;

    String description;

    @NotNull
    UUID areaId;

    Double lat;

    Double lon;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    String emergencyPhone;
}
