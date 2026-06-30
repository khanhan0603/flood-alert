package com.example.flood_alert.dbo.response;

import java.time.LocalDate;
import java.util.UUID;

import com.example.flood_alert.enums.Role;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyProfileResponse {
    UUID id;
    String hoten;
    Boolean gioitinh;
    LocalDate ngaysinh;
    String sodt;
    String diachi;
    String email;
    String ghichu;

    String area;

    Role role;

    // Chức vụ hiển thị
    String chucVu;

    // Chỉ có khi là RESCUER
    String rescueTeam;
    String rescueGroup;

    // Chỉ có khi là PROVINCE_OPERATOR
    String province;
}
