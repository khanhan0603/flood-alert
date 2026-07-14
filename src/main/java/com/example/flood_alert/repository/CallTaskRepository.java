package com.example.flood_alert.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.CallTask;
import com.example.flood_alert.enums.CallTaskStatus;

import io.lettuce.core.dynamic.annotation.Param;

public interface CallTaskRepository extends JpaRepository<CallTask, UUID> {

    // Lấy CallTask của một sos
    @Query("""
                SELECT ct
                FROM CallTask ct
                WHERE ct.sosRequest.id = :sosId
            """)
    Optional<CallTask> findBySosId(@Param("sosId") UUID sosId);

    // Lấy CallTask của một support request
    Optional<CallTask> findBySupportRequestId(UUID supportRequestId);

    // Lấy danh sách calltask theo các trạng thái
    List<CallTask> findByStatusIn(Collection<CallTaskStatus> statuses);

    // Kiểm tra một User hiện đang được hệ thống gọi những CallTask nào.
    List<CallTask> findByTargetUserId(UUID targetUserId);

    // Kiểm tra SOS này còn Call Workflow đang chạy hay không.
    boolean existsBySosRequestIdAndStatusIn(UUID sosRequestId, Collection<CallTaskStatus> statuses);

    // Kiểm tra support request này còn Call Workflow đang chạy hay không.
    boolean existsBySupportRequestIdAndStatusIn(UUID supportRequestId, Collection<CallTaskStatus> statuses);

    Optional<CallTask> findFirstByAssignment_IdOrderByCreatedAtDesc(UUID assignmentId);

}
