package com.example.flood_alert.dbo.response;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamMemberDetailResponse {
    UUID id;
    String hoten;
    Boolean gioitinh;
    LocalDate ngaysinh;
    String sodt;
    String diachi;
    String email;
    String ghichu;
    String role;
    String tenDoiTrucThuoc;
    String tenNhomPhuTrach;
    String trangThaiHoatDong;
}
