package com.example.flood_alert.dbo.request;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRescueTeamRequest {
    @NotBlank(message = "NAME_TEAM_REQUIRED")
    String name;

    String description;

    @Pattern(regexp = "^0[0-9]{9}$",message = "INVALID_PHONE")
    String emergencyPhone;

    BigDecimal lat;

    BigDecimal lon;
}
