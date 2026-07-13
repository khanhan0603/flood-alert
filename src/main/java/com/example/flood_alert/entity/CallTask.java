package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.flood_alert.enums.CallTargetType;
import com.example.flood_alert.enums.CallTaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "call_tasks", indexes = {
        // Tìm CallTask theo SOS
        @Index(name = "idx_call_task_sos_request", columnList = "sos_request_id"),

        // Tìm CallTask theo Support Request
        @Index(name = "idx_call_task_support_request", columnList = "support_request_id"),

        // Scheduler thường tìm các task theo trạng thái
        @Index(name = "idx_call_task_status", columnList = "status"),

        // Kiểm tra các cuộc gọi đang xử lý của một User
        @Index(name = "idx_call_task_target_user", columnList = "target_user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallTask extends BaseEntity {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // Người hiện tại hệ thống đang gọi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    User targetUser;

    // Loại đối tượng đang được gọi
    // Ví dụ:
    // TEAM_LEADER
    // DEPUTY_LEADER
    // PROVINCE_OPERATOR
    // GROUP_LEADER
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 40)
    CallTargetType targetType;

    // Đã retry bao nhiêu lần
    @Column(name = "retry_count", nullable = false)
    Integer retryCount;

    // Thời gian chờ phản hồi (giây)
    @Column(name = "timeout_seconds", nullable = false)
    Integer timeoutSeconds;

    // Bước hiện tại của Call Workflow
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    CallTaskStatus status;

    // Liên kết với SOS nếu đây là CallTask của SOS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_request_id")
    SosRequest sosRequest;

    // Liên kết với Support Request nếu đây là CallTask của Support Request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_request_id")
    SupportRequest supportRequest;
}