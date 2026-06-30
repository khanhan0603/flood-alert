package com.example.flood_alert.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.flood_alert.configuration.SmsProperties;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SmsService {
    SmsProperties smsProperties;
    RestClient restClient;

    public String getUserInfo() {
        String authString = smsProperties.getAccessToken() + ":x";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(authString.getBytes(StandardCharsets.UTF_8));

        return restClient.get()
                .uri("https://api.speedsms.vn/index.php/user/info")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .retrieve()
                .body(String.class);
    }
}