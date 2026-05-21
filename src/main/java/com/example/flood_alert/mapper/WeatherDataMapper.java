package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.flood_alert.dbo.request.WeatherDataCreationRequest;
import com.example.flood_alert.entity.WeatherData;

@Mapper(componentModel = "spring")
public interface WeatherDataMapper {
    @Mapping(source = "precipitation", target="rainfall")
    @Mapping(source = "temperature2m", target="temperature")
    @Mapping(source = "dewpoint2m", target="dewpoint")
    @Mapping(source = "surfacePressure", target="pressure")
    @Mapping(source = "windspeed10m", target="wind_speed")
    @Mapping(source = "winddirection10m", target="wind_direction")
    @Mapping(source = "relativehumidity2m", target="humidity")
    @Mapping(source = "evapotranspiration", target="evapotranspiration")
    WeatherData toWeatherData(WeatherDataCreationRequest weatherDataCreationRequest);
}
