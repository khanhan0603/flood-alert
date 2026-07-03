package com.example.flood_alert.dbo.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateRescueGroupRequest {

    String name;

    Boolean hasBoat;

    Boolean hasMedical;

    Boolean hasSearchRescue;

    Boolean hasLogistics;

    String notes;
}