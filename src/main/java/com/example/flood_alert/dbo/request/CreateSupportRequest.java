package com.example.flood_alert.dbo.request;
import java.util.UUID;

import com.example.flood_alert.enums.SupportType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Team leader tạo support request
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSupportRequest {
    UUID sosId;

    SupportType supportType;

    UUID suggestedGroupId;

    String reason;
}
