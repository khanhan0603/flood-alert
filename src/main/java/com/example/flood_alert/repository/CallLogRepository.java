package com.example.flood_alert.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.CallLog;

public interface CallLogRepository extends JpaRepository<CallLog, UUID> {

    //Xem toàn bộ lịch sử cuộc gọi của một SOS theo đúng thứ tự thời gian.
    List<CallLog> findBySosRequestIdOrderByStartedAtAsc(UUID sosRequestId);

    //Xem toàn bộ lịch sử cuộc gọi của một Support Request.
    List<CallLog> findBySupportRequestIdOrderByStartedAtAsc(UUID supportRequestId);

}