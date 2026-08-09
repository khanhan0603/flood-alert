package com.example.flood_alert.dbo.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message="VICTIM_COUNT_REQUIRED")
    @Min(value=1, message="VICTIM_COUNT_INVALID")
    Integer victimCount;

    @NotNull(message = "LAT_REQUIRED")
    @DecimalMin(value = "-90.0",message = "LAT_INVALID")
    @DecimalMax(value = "90.0",message = "LAT_INVALID")
    BigDecimal lat;

    @NotNull(message = "LON_REQUIRED")
    @DecimalMin(value="-180.0", message = "LON_INVALID")
    @DecimalMax(value="180.0", message = "LON_INVALID")
    BigDecimal lon;

    @NotBlank(message="LOCATION_REQUIRED")
    @Size(max=500, message="ADDRESS_TOO_LONG")
    String diachi;

    @Min(0)
    Double accuracy;

    @NotNull
    Boolean injured;

    @NotNull
    Boolean trapped;

    @NotNull
    Boolean vulnerable;

    @Size(max = 1000,message="DESCRIPTION_TOO_LONG")
    String mota;
}