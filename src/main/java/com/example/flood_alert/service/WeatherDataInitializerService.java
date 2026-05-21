package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.flood_alert.dbo.request.WeatherDataCreationRequest;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.WeatherData;
import com.example.flood_alert.mapper.WeatherDataMapper;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.WeatherDataRepository;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE, makeFinal=true)
public class WeatherDataInitializerService {
    static final String ARCHIVE_URL=
        "https://archive-api.open-meteo.com/v1/archive";//endpoint lấy dữ liệu thời tiết trong quá khứ
    static final String FORECAST_URL=
        "https://api.open-meteo.com/v1/forecast";//endpoint lấy dữ liệu thời tiết trong hiện tại
    static final String TIMEZONE="Asia/Bangkok";
    static final String HOURLY_FIELDS=
        "precipitation,temperature_2m,dewpoint_2m,surface_pressure,windspeed_10m,winddirection_10m,relativehumidity_2m,et0_fao_evapotranspiration";

    WeatherDataRepository weatherDataRepository;
    AreaRepository areaRepository;
    WeatherDataMapper weatherDataMapper;
    RestTemplateBuilder restTemplateBuilder;
    AtomicBoolean backfilled=new AtomicBoolean(false);


    public void backfill(int limit){
        if(backfilled.get()){
            return;
        }

        LocalDate end=LocalDate.now();
        LocalDate start=end.minusDays(30);
        List<Area> areas=getAreasWithLocation();
        if(areas.isEmpty()){
            log.info("SKIP WEATHER BACKFILL: NO AREA WITH LAT/LON");
            return;
        }
        fetchAndSaveArchive(areas,start,end);
        backfilled.set(true);
        log.info("BACKFILL WEATHER DONE: {} -> {}",start,end);
    }

    @Scheduled(cron="0 0 * * * *")
    public void fetchHourly(){
        List<Area> areas=getAreasWithLocation();

        if(areas.isEmpty()){
            log.info("SKIP HOURLY WEATHER FETCH: NO AREA WITH LAT/LON");
            return;
        }

        fetchAndSaveCurrent(areas);
    }

    private void fetchAndSaveCurrent(List<Area> areas) {
        RestTemplate restTemplate=restTemplateBuilder.build();
        for(Area area:areas){
            String url=UriComponentsBuilder
                .fromUriString(FORECAST_URL)
                .queryParam("latitude",area.getLat())
                .queryParam("longitude", area.getLon())
                .queryParam("current", HOURLY_FIELDS)
                .queryParam("timezone", TIMEZONE)
                .toUriString();
            try {
                JsonNode response=restTemplate.getForObject(
                    url,
                    JsonNode.class
                );
                if(response==null){
                    continue;
                }
                saveHourlyData(area,response.path("current"));
            } catch (Exception e) {
                log.error("ERROR FETCH CURRENT WEATHER FOR AREA {}",area.getId(),e);
            }
        }
    }

    private void fetchAndSaveArchive(List<Area> areas, LocalDate start, LocalDate end) {
       RestTemplate restTemplate=restTemplateBuilder.build();

       for(Area area:areas){
            String url=UriComponentsBuilder
                .fromUriString(ARCHIVE_URL)
                .queryParam("latitude",area.getLat())
                .queryParam("longitude", area.getLon())
                .queryParam("start_date", start)
                .queryParam("end_date", end)
                .queryParam("hourly", HOURLY_FIELDS)
                .queryParam("timezone", TIMEZONE)
                .toUriString();
            log.info("URL = {}", url);
            try{
                JsonNode response=restTemplate.getForObject(
                                                url, JsonNode.class);
                if(response==null){
                    continue;
                }
                saveHourlyData(area,response.path("hourly"));
            }
            catch(Exception e){
                log.error("ERROR FETCH ARCHIVE WEATHER FOR AREA {}",area.getId(),e);
            }
       }
    }

    private void saveHourlyData(Area area, JsonNode hourly) {
        JsonNode times=hourly.path("time");
        if(!times.isArray()){
            return;
        }
        List<WeatherData> weatherDatas=new ArrayList<>();
        for(int i=0;i<times.size();i++){
            LocalDateTime time=LocalDateTime.parse(times.get(i).asText());
            if(weatherDataRepository.existsByAreaAndTime(area, time)){
                continue;
            }
            WeatherDataCreationRequest request=WeatherDataCreationRequest.builder()
                .precipitation(decimal(hourly,"precipitation",i))
                .temperature2m(decimal(hourly,"temperature_2m",i))
                .dewpoint2m(decimal(hourly,"dewpoint_2m",i))
                .surfacePressure(decimal(hourly,"surface_pressure",i))
                .windspeed10m(decimal(hourly,"windspeed_10m",i))
                .winddirection10m(decimal(hourly,"winddirection_10m",i))
                .relativehumidity2m(decimal(hourly,"relativehumidity_2m",i))
                .evapotranspiration(decimal(hourly,"et0_fao_evapotranspiration",i))
                .lat(area.getLat())
                .lon(area.getLon())
                .build();
            
            WeatherData weatherData=weatherDataMapper.toWeatherData(request);
            weatherData.setArea(area);
            weatherData.setTime(time);
            weatherDatas.add(weatherData);

        }
        weatherDataRepository.saveAll(weatherDatas);
    }

    private List<Area> getAreasWithLocation() {
        return areaRepository
            .findAll()
            .stream()
            .filter(area ->
                area.getLevel()==2
                &&area.getLat()!=null
                && area.getLon()!=null
            )
            .limit(2)
            .toList();
    }
    private BigDecimal decimal(JsonNode node, String field,int index){
        JsonNode values=node.path(field);
        if(!values.isArray()||index>=values.size()||values.get(index).isNull()){
            return null;
        }
        return BigDecimal.valueOf(values.get(index).asDouble());
    }
}
