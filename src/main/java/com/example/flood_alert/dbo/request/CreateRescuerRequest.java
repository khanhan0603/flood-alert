package com.example.flood_alert.dbo.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRescuerRequest {

    @NotBlank(message = "HOTEN_REQUIRED")
    private String hoten;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "SODT_REQUIRED")
    @Pattern(regexp = "^0[0-9]{9}$",message = "INVALID_PHONE")
    private String sodt;

    @NotNull(message = "GIOITINH_REQUIRED")
    private Boolean gioitinh;

    @NotNull(message = "NGAYSINH_REQUIRED")
    @Past(message = "BIRTH_DATE_INVALID")
    private LocalDate ngaysinh;

    @NotBlank(message = "LOCATION_REQUIRED")
    private String diachi;

    @NotNull(message = "TEAM_REQUIRED")
    private UUID teamId;
}