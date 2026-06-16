package com.example.flood_alert.dbo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSosRequest {

    String sodt;

    @NotNull
    @Min(1)
    Integer victimCount;

    @NotNull
    Double lat;

    @NotNull
    Double lon;

    String diachi;

    Double accuracy;

    @NotNull
    Boolean injured;

    @NotNull
    Boolean trapped;

    @NotNull
    Boolean vulnerable;

    String mota;
}