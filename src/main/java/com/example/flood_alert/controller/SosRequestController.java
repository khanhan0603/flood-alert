package com.example.flood_alert.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.CreateSosRequest;

import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.service.SOSRequestService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sos-request")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SosRequestController {
    SOSRequestService sosRequestService;

    @PostMapping
    public ApiResponse<SosResponse> create(
            @RequestBody CreateSosRequest request) {

        return ApiResponse.<SosResponse>builder()
                .result(sosRequestService.create(request))
                .build();
    }
}
