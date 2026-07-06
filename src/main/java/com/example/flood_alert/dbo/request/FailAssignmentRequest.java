package com.example.flood_alert.dbo.request;

import com.example.flood_alert.enums.AssignmentFailedReason;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FailAssignmentRequest {

    @NotNull
    AssignmentFailedReason failedReason;

    String failedNote;
}