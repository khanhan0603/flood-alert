package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AlarmResponse {

    UUID id;

    //Tiêu đề cảnh báo
    String title;

    //Nội dung cảnh báo
    String message;

    //SOS phát sinh
    UUID sosRequestId;

    //Mã tra sos
    String trackingCode;

    //Call Workflow gây ra alarm
    UUID callTaskId;

    //Thời điểm Alarm được tạo
    LocalDateTime createdAt;
}