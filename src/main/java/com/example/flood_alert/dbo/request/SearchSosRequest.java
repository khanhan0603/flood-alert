package com.example.flood_alert.dbo.request;

import com.example.flood_alert.enums.StatusSOS;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchSosRequest {

    // Nội dung người dùng nhập trên ô tìm kiếm.
    // Có thể là:
    // - Số điện thoại
    // - Mã tracking
    String keyword;

    // Lọc theo trạng thái SOS (không bắt buộc)
    StatusSOS status;
}