package com.example.flood_alert.dbo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateRescueGroupRequest {

    @NotBlank(message = "NAME_GROUP_REQUIRED")
    String name;

    Boolean hasBoat;

    Boolean hasMedical;

    Boolean hasSearchRescue;

    Boolean hasLogistics;

    String notes;
}