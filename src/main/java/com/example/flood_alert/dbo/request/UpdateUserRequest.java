package com.example.flood_alert.dbo.request;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserRequest {
    String hoten;
    Boolean gioitinh;
    LocalDate ngaysinh;
    String sodt;
    String diachi;
    String email;
    String ghichu;
    // Chỉ CITIZEN mới được sử dụng
    String areaId;
}
