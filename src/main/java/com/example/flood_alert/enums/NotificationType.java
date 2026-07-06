package com.example.flood_alert.enums;

//tập hợp các thông báo về nhiệm vụ: group thất bại, sos quá hạn, yêu cầu hỗ trợ...
public enum NotificationType {

    // SOS
    SOS_ASSIGNED,
    SOS_OVERDUE,

    // Assignment
    ASSIGNMENT_FAILED,

    // Support Request
    SUPPORT_REQUEST_CREATED,      // Team Leader gửi yêu cầu chi viện

    SUPPORT_REQUEST_APPROVED,     // Province duyệt

    SUPPORT_REQUEST_REJECTED,     // Province từ chối

    SUPPORT_ASSIGNMENT_ASSIGNED,  // Province điều động Team khác

    SUPPORT_ASSIGNMENT_REJECTED,  // Team được điều động từ chối

    // Khác
    SYSTEM
}