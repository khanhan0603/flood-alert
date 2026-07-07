package com.example.flood_alert.dbo.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

// Tạo Request cho Group Leader gửi yêu cầu hỗ trợ
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateGroupSupportRequest {
    @NotBlank
    String reason;

    List<CreateSupportRequestItem> items;
}
