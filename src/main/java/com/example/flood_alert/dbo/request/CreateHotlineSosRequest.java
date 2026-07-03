package com.example.flood_alert.dbo.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateHotlineSosRequest {

    // EmergencyCallEvent được chọn.
    // Null nếu Operator nhập tay toàn bộ thông tin.
    UUID callEventId;

    // Số điện thoại người cần cứu hộ.
    // Chỉ bắt buộc khi emergencyCallEventId = null.
    String sodt;

    // Chỉ dùng khi Operator nhập tay.
    BigDecimal lat;

    BigDecimal lon;

    // Địa chỉ người dân đọc qua điện thoại.
    String rawAddressText;

    @NotNull
    @Min(1)
    Integer victimCount;

    @NotNull
    Boolean injured;

    @NotNull
    Boolean trapped;

    @NotNull
    Boolean vulnerable;

    String mota;

}