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
    INVALID_PASSWORD(1002,"Mật khẩu phải ít nhất 6 ký tự!"),
    INVALID_EMAIL(1001,"Email không hợp lệ! Dạng email@gmail.com!"),
    INVALID_PHONE(1006,"Số điện thoại không hợp lệ! Dạng 0xxxxxxxxx!"),
    EMAIL_EXISTED(1007,"Email này đã được đăng ký! Vui lòng dùng email khác!"),
    PHONE_EXISTED(1008,"Số điện thoại này đã được đăng ký! Vui số dùng số khác!"),
    AREA_NOT_FOUND(1011,"Không tìm thấy khu vực phù hợp!"),
    DEVICE_CODE_EXISTED(1012,"Mã thiết bị này đã tồn tại!"),
    DEVICE_NOT_FOUND(1013,"Không tìm thấy thiết bị!"),
    DEVICE_ALREADY_PROCESSED(1014,"Thiết bị này đã được xử lý!"),
    USER_NOT_EXISTED(1015,"Không tìm thấy người dùng!"),
    ;
    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
