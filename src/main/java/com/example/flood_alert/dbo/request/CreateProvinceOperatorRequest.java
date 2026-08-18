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
public class CreateProvinceOperatorRequest {

    @NotBlank(message = "HOTEN_REQUIRED")
    private String hoten;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "SODT_REQUIRED")
    @Pattern(regexp = "^0\\d{9}$", message = "INVALID_PHONE")
    private String sodt;

    @NotNull(message = "GIOITINH_REQUIRED")
    private Boolean gioitinh;

    @NotNull(message = "NGAYSINH_REQUIRED")
    @Past(message = "BIRTH_DATE_INVALID") // @Past dùng để kiểm tra một giá trị ngày/thời gian phải nằm trong quá khứ.
    private LocalDate ngaysinh;
    
    private String diachi;

    @NotNull(message = "WORK_AREA_REQUIRED")
    private UUID areaId;
}