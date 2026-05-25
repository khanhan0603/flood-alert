package com.example.flood_alert.configuration;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.flood_alert.service.AreaDataInitializerService;
import com.example.flood_alert.service.UserDataInitializerService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Configuration
public class ApplicationInitConfig {

    AreaDataInitializerService areaDataInitializerService;
    UserDataInitializerService userDataInitializerService;
    //StringRedisTemplate stringRedisTemplate;

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            areaDataInitializerService.init();
            userDataInitializerService.init();

            // Reset Redis weather keys để backfill chạy lại từ đầu
            // trigger redeploy
            // stringRedisTemplate.delete("weather:backfill_done");
            // stringRedisTemplate.delete("weather:last_area_id");
            // log.info("RESET WEATHER REDIS KEYS");
        };
    }
}