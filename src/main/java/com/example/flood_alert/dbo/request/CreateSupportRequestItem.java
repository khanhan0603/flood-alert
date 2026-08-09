package com.example.flood_alert.dbo.request;

import com.example.flood_alert.enums.SupportType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSupportRequestItem {

    @NotNull(message = "SUPPORT_TYPE_REQUIRED")
    SupportType supportType;

    @NotNull(message = "REQUIRED_GROUP_COUNT_REQUIRED")
    @Min(value = 1, message = "REQUIRED_GROUP_COUNT_INVALID")
    Integer requiredGroupCount;
}