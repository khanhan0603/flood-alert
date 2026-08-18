package com.example.flood_alert.dbo.request;

import java.time.LocalDate;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level=AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "HOTEN_REQUIRED")
    String hoten;
    @NotNull(message = "GIOITINH_REQUIRED")
    Boolean gioitinh;
    @NotNull(message = "NGAYSINH_REQUIRED")
    @Past(message = "BIRTH_DATE_INVALID")
    LocalDate ngaysinh;
    @NotBlank(message = "LOCATION_REQUIRED")
    String diachi;
    @NotBlank(message = "SODT_REQUIRED")
    @Pattern(regexp = "^0[0-9]{9}$", message = "INVALID_PHONE")
    String sodt;
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    String email;
    @Size(min=6,message="INVALID_PASSWORD")
    String password;
    String area_id;
    String ghichu;
}
