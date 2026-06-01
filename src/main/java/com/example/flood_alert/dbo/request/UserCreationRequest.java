package com.example.flood_alert.dbo.request;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import javax.validation.constraints.Size;
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
    String hoten;
    boolean gioitinh;
    LocalDate ngaysinh;
    String diachi;
    String sodt;
    String email;
    @Size(min=6,message="INVALID_PASSWORD")
    String password;
    String area_id;
}
