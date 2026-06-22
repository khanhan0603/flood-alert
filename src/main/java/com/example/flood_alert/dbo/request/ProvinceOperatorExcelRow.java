package com.example.flood_alert.dbo.request;
import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProvinceOperatorExcelRow {
    int rowNumber;
    String hoten;
    Boolean gioitinh;
    LocalDate ngaysinh;
    String sodt;
    String email;
    String diachi;
    String provinceName;
}
