package com.example.flood_alert.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.flood_alert.configuration.SmsProperties;
import com.example.flood_alert.dbo.request.SmsRequest;

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

    public void sendSms(String phone, String content) {

        SmsRequest request = SmsRequest.builder()
                .apiKey(smsProperties.getApiKey())
                .secretKey(smsProperties.getSecretKey())
                .phone(phone)
                .content(content)
                .smsType(2)
                .build();

        String response = restClient.post()
                .uri("https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_post_json/")
                .body(request)
                .retrieve()
                .body(String.class);

        log.info("SMS Response: {}", response);
    }

}
