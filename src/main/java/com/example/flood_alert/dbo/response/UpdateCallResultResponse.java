package com.example.flood_alert.dbo.response;

import java.util.UUID;

import com.example.flood_alert.enums.CallTargetType;
import com.example.flood_alert.enums.CallTaskStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

// Gửi FE để biết có cần thực hiện gọi tiếp hay ko
//nếu cần thì gọi ai
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCallResultResponse {
    // CallTask hiện tại
    UUID callTaskId;

    // Trạng thái CallTask sau khi xử lý
    CallTaskStatus status;

    // Người FE cần gọi tiếp.
    // Null nếu Call Workflow đã SUCCESS hoặc FAILED.
    UUID targetUserId;

    // Tên người cần gọi
    String targetUserName;

    // Số điện thoại cần gọi tiếp
    String phoneNumber;

    // TEAM_LEADER / DEPUTY_LEADER / PROVINCE_OPERATOR...
    CallTargetType targetType;

    // Retry hiện tại của target này
    Integer retryCount;

    // Thời gian timeout của cuộc gọi
    Integer timeoutSeconds;
}
