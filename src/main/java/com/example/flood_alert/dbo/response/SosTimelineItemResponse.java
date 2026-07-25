package com.example.flood_alert.dbo.response;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.StatusSOS;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SosTimelineItemResponse {
    StatusSOS status;
    LocalDateTime updatedAt;
    String note;
}