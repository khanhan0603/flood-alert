package com.example.flood_alert.dbo.request;
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
    @NotBlank
    String name;

    String description;

    @Pattern(
        regexp = "^(0|\\+84)[0-9]{9,10}$",
        message = "Số điện thoại không hợp lệ")
    String emergencyPhone;

    Double lat;

    Double lon;
}
