package com.example.flood_alert.dbo.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProvinceOperatorRequest {
    UUID id;
    @NotBlank(message = "HOTEN_REQUIRED")
    String hoten;
    @NotNull(message = "GIOITINH_REQUIRED")
    Boolean gioitinh;
    @NotNull(message = "NGAYSINH_REQUIRED")
    @Past(message = "BIRTH_DATE_INVALID")
    LocalDate ngaysinh;
    @NotBlank(message = "SODT_REQUIRED")
    @Pattern(regexp = "^0[0-9]{9}$", message = "INVALID_PHONE")
    String sodt;

    String diachi;
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    String email;
    String ghichu;

    UUID areaId;
}
