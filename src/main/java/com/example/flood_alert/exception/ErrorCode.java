package com.example.flood_alert.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized error"),
    INVALID_KEY(1005,"Invalid message key"),
    LOGIN_INFO_EXISTED(1009,"Thông tin đăng nhập không chính xác"),
    UNAUTHENTICATED(1010,"Xác thực không hợp lệ"),
    EMPTY_AREA_BY_PARENT_ID(1004,"Không tìm thấy danh sách phường/xã hợp lệ!"),
    EMPTY_POLYGIN_BY_ID(1003,"Không tìm thấy ranh giới hợp lệ!"),
    ;
    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
