package com.example.flood_alert.dbo.request;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level=AccessLevel.PRIVATE)
public class UpdateProvinceOperatorRequest {
    UUID id;
    String hoten;
    Boolean gioitinh;
    LocalDate ngaysinh;
    String sodt;
    String diachi;
    String email;
    String ghichu;
    UUID areaId;
}
