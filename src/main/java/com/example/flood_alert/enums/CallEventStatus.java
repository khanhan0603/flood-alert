package com.example.flood_alert.enums;

public enum CallEventStatus {
    PENDING_MATCH, // đã hiển thị số cho dân, chưa có SOS nào được tạo dựa trên lần bấm này
    MATCHED, // Operator đã đối chiếu và tạo SOS từ event này (đổi tên từ CONVERTED)
    STALE // quá mốc thời gian hợp lý, không còn hiển thị cho Operator đối chiếu nữa
}
