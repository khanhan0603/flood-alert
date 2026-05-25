package com.example.flood_alert.service;

import org.springframework.stereotype.Service;

import com.example.flood_alert.repository.WeatherDataRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
@RequiredArgsConstructor
public class WeatherDataService {
    final WeatherDataRepository weatherDataRepository;

    public String deleteScheduledWeather() {
        long count=weatherDataRepository.countAreaWithoutWeatherData();
        String alert="";
        if(count==3321){
            alert="Import đầy đủ dữ liệu thời tiết. "+count+" phường/xã";
        }
        else{
            alert="Import dữ liệu thời tiết. "+count+" phường/xã";
        }
        return alert;
    }
}
