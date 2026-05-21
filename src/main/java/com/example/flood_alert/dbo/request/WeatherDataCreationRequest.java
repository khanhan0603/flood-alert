package com.example.flood_alert.dbo.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeatherDataCreationRequest {
    @JsonProperty("precipitation")
    BigDecimal precipitation;

    @JsonProperty("temperature_2m")
    BigDecimal temperature2m;

    @JsonProperty("dewpoint_2m")
    BigDecimal dewpoint2m;

    @JsonProperty("surface_pressure")
    BigDecimal surfacePressure;

    @JsonProperty("windspeed_10m")
    BigDecimal windspeed10m;

    @JsonProperty("winddirection_10m")
    BigDecimal winddirection10m;

    @JsonProperty("relativehumidity_2m")
    BigDecimal relativehumidity2m;

    @JsonProperty("et0_fao_evapotranspiration")
    BigDecimal evapotranspiration;
    
    BigDecimal lat;
  
    BigDecimal lon;
}
