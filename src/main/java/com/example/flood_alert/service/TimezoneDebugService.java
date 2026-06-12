package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TimezoneDebugService {

    @PostConstruct
    public void init() {

        log.info("ZONE={}", ZoneId.systemDefault());

        log.info("USER.TIMEZONE={}",
                System.getProperty("user.timezone"));

        log.info("NOW={}",
                LocalDateTime.now());
    }
}
