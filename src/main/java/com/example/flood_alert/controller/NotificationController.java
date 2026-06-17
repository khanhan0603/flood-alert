package com.example.flood_alert.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // @PostMapping("/token")
    // public String saveToken(
    //         @RequestParam UUID userId,
    //         @RequestBody SaveFcmTokenRequest request) {

    //     notificationService.saveToken(
    //             userId,
    //             request.getToken());

    //     return "OK";
    // }

}
