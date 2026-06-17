package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.enums.StatusSOS;

public interface SosRequestRepository extends JpaRepository<SosRequest, UUID> {
        // Kiểm tra người dân đã đăng nhập đã gửi yêu cầu SOS chưa
        boolean existsByUserIdAndStatusIn(UUID userId, List<StatusSOS> statuses);

        // Kiểm tra người dân chưa đăng nhập đã gửi yêu cầu SOS chưa
        boolean existsBySodtAndClientDeviceIdAndStatusIn(String sodt, String clientDeviceId, List<StatusSOS> statuses);

        // Cập nhật trang thái yêu cầu SOS
        Optional<SosRequest> findFirstByUserIdAndStatusIn(
                        UUID userId,
                        List<StatusSOS> statuses);

        Optional<SosRequest> findFirstBySodtAndClientDeviceIdAndStatusIn(
                        String sodt,
                        String clientDeviceId,
                        List<StatusSOS> statuses);

        //Tìm kiếm yêu cầu SOS theo Id và UserId
        Optional<SosRequest> findByIdAndUserId(
                        UUID sosId,
                        UUID userId);

        //Tìm kiếm yêu cầu SOS theo Id và Sodt và ClientDeviceId
        Optional<SosRequest> findByIdAndSodtAndClientDeviceId(
                        UUID sosId,
                        String sodt,
                        String clientDeviceId);
}