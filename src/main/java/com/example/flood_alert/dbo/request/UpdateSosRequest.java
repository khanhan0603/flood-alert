package com.example.flood_alert.dbo.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSosRequest {

    @NotNull
    @Min(1)
    Integer victimCount;

    @NotNull
    BigDecimal lat;

    @NotNull
    BigDecimal lon;

    String diachi;

    @Min(0)
    Double accuracy;

    @NotNull
    Boolean injured;

    @NotNull
    Boolean trapped;

    @NotNull
    Boolean vulnerable;

    @Size(max = 1000)
    String mota;
}