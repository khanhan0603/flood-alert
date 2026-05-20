package com.example.flood_alert.configuration;

import java.util.UUID;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.flood_alert.service.AreaDataInitializerService;
import com.example.flood_alert.service.UserDataInitializerService;
import com.fasterxml.uuid.Generators;

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

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            areaDataInitializerService.init();
            userDataInitializerService.init();
        };
    }}
