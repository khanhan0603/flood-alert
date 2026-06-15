package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.SaveFcmTokenRequest;
import com.example.flood_alert.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    public String saveToken(
            @RequestParam UUID userId,
            @RequestBody SaveFcmTokenRequest request) {

        notificationService.saveToken(
                userId,
                request.getToken());

        return "OK";
    }

}
