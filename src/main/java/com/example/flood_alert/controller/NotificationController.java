package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.SaveFcmTokenRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.PopupNotificationResponse;
import com.example.flood_alert.service.NotificationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    // @PostMapping("/test")
    // public String test(@RequestParam String token) throws Exception {

    // notificationService.sendNotification(
    // token,
    // "Test Flood Alert",
    // "Đây là thông báo thử nghiệm.");

    // return "OK";
    // }

    @PostMapping("/token")
    public void saveToken(@RequestBody SaveFcmTokenRequest request) {
        notificationService.saveToken(request.getToken());
    }

    // Danh sách các popup đang PENDING
    @GetMapping("/popup")
    public ApiResponse<List<PopupNotificationResponse>> getMyPopupNotifications() {

        return ApiResponse.<List<PopupNotificationResponse>>builder()
                .result(notificationService.getMyPopupNotifications())
                .build();
    }

    // Đánh dấu đã đọc popup
    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable UUID notificationId) {

        notificationService.markAsRead(notificationId);

        return ApiResponse.<Void>builder()
                .build();
    }
}
