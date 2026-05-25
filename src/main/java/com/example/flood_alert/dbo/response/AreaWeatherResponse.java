package com.example.flood_alert.dbo.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaWeatherResponse {
    UUID id;
    String tenkhuvuc;
    
}
