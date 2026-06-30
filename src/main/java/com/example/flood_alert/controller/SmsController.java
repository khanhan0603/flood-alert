package com.example.flood_alert.controller;

import org.springframework.web.bind.annotation.*;

import com.example.flood_alert.dbo.request.SmsTestRequest;
import com.example.flood_alert.service.SmsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/test")
    public void test(@RequestBody SmsTestRequest request) {

        smsService.sendSms(
                request.getPhone(),
                request.getContent());
    }
}