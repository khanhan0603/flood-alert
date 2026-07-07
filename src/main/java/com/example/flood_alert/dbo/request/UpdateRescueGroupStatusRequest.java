package com.example.flood_alert.dbo.request;

import com.example.flood_alert.enums.RescueGroupStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRescueGroupStatusRequest {

    @NotNull
    RescueGroupStatus status;
}