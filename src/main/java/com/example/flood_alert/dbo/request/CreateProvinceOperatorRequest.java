package com.example.flood_alert.dbo.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProvinceOperatorRequest {

    @NotBlank(message = "Họ tên không được để trống!")
    private String hoten;

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Email không hợp lệ!")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống!")
    private String sodt;

    @NotNull(message = "Giới tính không được để trống!")
    private Boolean gioitinh;

    @NotNull(message = "Ngày sinh không được để trống!")
    private LocalDate ngaysinh;

    @NotBlank(message = "Địa chỉ không được để trống!")
    private String diachi;

    @NotNull(message = "Khu vực phụ trách không được để trống!")
    private UUID areaId;
}