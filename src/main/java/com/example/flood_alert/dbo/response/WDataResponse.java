package com.example.flood_alert.dbo.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WDataResponse {
    
    BigDecimal rainfall;

    BigDecimal temperature;

    BigDecimal dewpoint;

    BigDecimal pressure;
    
    BigDecimal wind_speed;

    BigDecimal wind_direction;

    BigDecimal humidity;

    BigDecimal evapotranspiration;

    LocalDateTime time;

}
