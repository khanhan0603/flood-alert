package com.example.flood_alert.dbo.response;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flood_alert.enums.NotificationType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Các thông báo popup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PopupNotificationResponse {
    UUID id;

    String title;

    String message;

    NotificationType type;

    UUID sosId;

    String trackingCode;

    LocalDateTime createdAt;

}
