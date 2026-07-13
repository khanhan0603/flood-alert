package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.flood_alert.enums.CallResult;
import com.example.flood_alert.enums.CallType;

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
@Table(name = "call_logs", indexes = {
        @Index(name = "idx_call_log_sos_request", columnList = "sos_request_id"),
        @Index(name = "idx_call_log_support_request", columnList = "support_request_id"),
        @Index(name = "idx_call_log_receiver", columnList = "receiver_user_id"),
        @Index(name = "idx_call_log_call_type", columnList = "call_type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallLog extends BaseEntity {

    // Người thực hiện cuộc gọi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_user_id", nullable = false)
    User callerUser;

    // Người nhận cuộc gọi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    User receiverUser;

    // Số điện thoại được gọi
    @Column(name = "phone_number", nullable = false, length = 20)
    String phoneNumber;

    // Loại cuộc gọi
    @Enumerated(EnumType.STRING)
    @Column(name = "call_type", nullable = false, length = 30)
    CallType callType;

    // Kết quả cuộc gọi
    @Enumerated(EnumType.STRING)
    @Column(name = "call_result", nullable = false, length = 30)
    CallResult callResult;

    // Lần gọi thứ mấy
    @Column(name = "attempt", nullable = false)
    Integer attempt;

    // Thời điểm bắt đầu cuộc gọi
    @Column(name = "started_at", nullable = false)
    LocalDateTime startedAt;

    // Thời điểm kết thúc cuộc gọi
    @Column(name = "ended_at")
    LocalDateTime endedAt;

    // Liên kết tới SOS
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_request_id")
    SosRequest sosRequest;

    // Liên kết tới Support Request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_request_id")
    SupportRequest supportRequest;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
