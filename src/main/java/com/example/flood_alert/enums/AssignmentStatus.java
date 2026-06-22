package com.example.flood_alert.enums;

public enum AssignmentStatus {
    ASSIGNED, // Team leader giao nhiệm vụ

    ACKNOWLEDGED, // Group leader xác nhận nhận nhiệm vụ

    MOVING, // Xuất phát

    ARRIVED, // Đã đến

    RESCUING, // Đang thực hiện cứu hộ

    COMPLETED, // Hoàn thành

    // FAILED dùng cho trường hợp:
    // xuồng hỏng
    // mất liên lạc
    // không tiếp cận được hiện trường
    // từ chối nhiệm vụ sau khi nhận
    FAILED
}
