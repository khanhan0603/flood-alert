package com.example.flood_alert.dbo.request;

import java.time.LocalDateTime;

import com.example.flood_alert.enums.CallResult;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

//Hotline cập nhật trạng thái cuộc gọi
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCallResultRequest {
    @NotNull(message = "CALL_RESULT_REQUIRED")
    CallResult callResult;

    // Thời điểm Hotline bắt đầu thực hiện cuộc gọi
    @NotNull(message = "STARTED_AT_REQUIRED")
    LocalDateTime startedAt;

    // Thời điểm Hotline kết thúc cuộc gọi
    @NotNull(message = "ENDED_AT_REQUIRED")
    LocalDateTime endedAt;

}
