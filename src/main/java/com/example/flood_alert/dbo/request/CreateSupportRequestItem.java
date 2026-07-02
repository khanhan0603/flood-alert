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

    @NotNull
    SupportType supportType;

    @NotNull
    @Min(1)
    Integer requiredGroupCount;
}