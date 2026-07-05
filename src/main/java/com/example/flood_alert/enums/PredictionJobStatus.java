package com.example.flood_alert.enums;

public enum PredictionJobStatus {
    SUCCESS, //schedule predict thành công
    PARTIAL_SUCCESS, //chạy thành công nhưng chưa hoàn toàn, cần recovery
    FAILED //schedule predict thất bại
}