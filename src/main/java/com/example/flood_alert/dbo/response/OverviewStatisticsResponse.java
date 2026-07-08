package com.example.flood_alert.dbo.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Response trả về các chỉ số tổng quan của hệ thống
 * phục vụ Dashboard Thống kê & Báo cáo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OverviewStatisticsResponse {

    long totalSos;

    long todaySos;

    long pendingSos;

    long assignedSos;

    long processingSos;

    long completedSos;

    long cancelledSos;

    long totalTeams;

    long totalGroups;

    long totalMembers;

    long totalDevices;
}