package com.example.flood_alert.dbo.request;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Email;
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
    String hoten;
    boolean gioitinh;
    LocalDate ngaysinh;
    String diachi;
    @Pattern(regexp="^(0|\\\\+84)[3|5|7|8|9][0-9]{8}$",
            message="INVALID_PHONE"
    )
    String sodt;
    @Email(message="INVALID_EMAIL")
    String email;
    @Size(min=6,message="INVALID_PASSWORD")
    String password;
    String area_id;
    String ghichu;
}
