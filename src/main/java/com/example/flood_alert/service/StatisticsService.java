package com.example.flood_alert.service;

import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.response.OverviewStatisticsResponse;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.repository.IoTDeviceRepository;
import com.example.flood_alert.repository.RescueGroupMemberRepository;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SosRequestRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Xử lý nghiệp vụ thống kê và báo cáo cho Dashboard Admin.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsService {

    SosRequestRepository sosRequestRepository;
    RescueTeamRepository rescueTeamRepository;
    RescueGroupRepository rescueGroupRepository;
    RescueGroupMemberRepository rescueGroupMemberRepository;
    IoTDeviceRepository ioTDeviceRepository;

    /**
     * Lấy dữ liệu thống kê tổng quan hiển thị trên Dashboard.
     */
    public OverviewStatisticsResponse getOverview() {

        return OverviewStatisticsResponse.builder()
                // Thống kê SOS
                .totalSos(sosRequestRepository.count())
                .todaySos(sosRequestRepository.countTodaySos())
                .pendingSos(sosRequestRepository.countByStatus(StatusSOS.PENDING))
                .assignedSos(sosRequestRepository.countByStatus(StatusSOS.ASSIGNED))
                .processingSos(sosRequestRepository.countByStatus(StatusSOS.PROCESSING))
                .completedSos(sosRequestRepository.countByStatus(StatusSOS.DONE))
                .cancelledSos(sosRequestRepository.countByStatus(StatusSOS.CANCELED))

                // Thống kê lực lượng cứu hộ
                .totalTeams(rescueTeamRepository.count())
                .totalGroups(rescueGroupRepository.count())
                .totalMembers(rescueGroupMemberRepository.count())

                // Thống kê thiết bị IoT
                .totalDevices(ioTDeviceRepository.count())
                .build();
    }
}