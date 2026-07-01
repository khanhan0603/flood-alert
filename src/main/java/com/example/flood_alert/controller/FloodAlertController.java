package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.FloodAlertResponse;
import com.example.flood_alert.service.AlertService;
import com.example.flood_alert.service.EmailProcessor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FloodAlertController {
    EmailProcessor emailProcessor;
    AlertService alertService;

    @PostMapping("/emails/process")
    public ApiResponse<Void> processEmails() {
        emailProcessor.processPendingEmails();
        return ApiResponse.<Void>builder()
                .message("Gửi email thành công")
                .build();
    }

    @GetMapping("/my-alerts/{userId}")
    public ApiResponse<Page<FloodAlertResponse>> getMyAlerts(@PathVariable UUID userId,@PageableDefault(page = 0,size = 10) Pageable pageable){

        return ApiResponse.<Page<FloodAlertResponse>>builder()
                .result(
                        alertService.getAlertsByUser(userId,pageable))
                .build();
    }

}
