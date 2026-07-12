package com.example.flood_alert.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.CallTask;
import com.example.flood_alert.enums.CallTaskStatus;

public interface CallTaskRepository extends JpaRepository<CallTask, UUID> {

    //Lấy CallTask của một sos
    Optional<CallTask> findBySosRequestId(UUID sosRequestId);

    //Lấy CallTask của một support request
    Optional<CallTask> findBySupportRequestId(UUID supportRequestId);

    //Lấy danh sách calltask theo các trạng thái
    List<CallTask> findByStatusIn(Collection<CallTaskStatus> statuses);

    //Kiểm tra các CallTask đang hướng đến một người dùng 
    //(phục vụ thống kê hoặc xử lý nghiệp vụ).
    List<CallTask> findByTargetUserId(UUID targetUserId);

}
