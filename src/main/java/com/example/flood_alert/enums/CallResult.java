package com.example.flood_alert.enums;

public enum CallResult {

    ANSWERED, //Người nhận đã xác nhận.

    NO_ANSWER, //Không bắt máy.

    REJECTED, //Từ chối nhận điều phối.

    TIMEOUT, //Hết thời gian chờ phản hồi.

    FAILED //Cuộc gọi lỗi (ví dụ lỗi hệ thống hoặc nhà mạng).

}
