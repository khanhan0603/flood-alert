package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.enums.SupportRequestStatus;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, UUID>{
    //Danh sách yêu cầu hỗ trợ theo trạng thái
    List<SupportRequest> findByStatus(SupportRequestStatus status);

    //Danh sách yêu cầu hỗ trợ theo sos id
    List<SupportRequest> findBySosId(UUID sosId);

    //Danh sách yêu cầu hỗ trợ theo Id yêu cầu
    List<SupportRequest> findByRequestedById(UUID requetedById);
}
