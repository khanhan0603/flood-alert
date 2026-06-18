package com.example.flood_alert.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    INVALID_KEY(1005, "Invalid message key"),
    LOGIN_INFO_EXISTED(1009, "Thông tin đăng nhập không chính xác"),
    UNAUTHENTICATED(1010, "Xác thực không hợp lệ"),
    EMPTY_AREA_BY_PARENT_ID(1004, "Không tìm thấy danh sách phường/xã hợp lệ!"),
    EMPTY_POLYGIN_BY_ID(1003, "Không tìm thấy ranh giới hợp lệ!"),
    INVALID_PASSWORD(1002, "Mật khẩu phải ít nhất 6 ký tự!"),
    INVALID_EMAIL(1001, "Email không hợp lệ! Dạng email@gmail.com!"),
    INVALID_PHONE(1006, "Số điện thoại không hợp lệ! Dạng 0xxxxxxxxx!"),
    EMAIL_EXISTED(1007, "Email này đã được đăng ký! Vui lòng dùng email khác!"),
    PHONE_EXISTED(1008, "Số điện thoại này đã được đăng ký! Vui số dùng số khác!"),
    AREA_NOT_FOUND(1011, "Không tìm thấy khu vực phù hợp!"),
    DEVICE_CODE_EXISTED(1012, "Mã thiết bị này đã tồn tại!"),
    DEVICE_NOT_FOUND(1013, "Không tìm thấy thiết bị!"),
    DEVICE_ALREADY_PROCESSED(1014, "Thiết bị này đã được xử lý!"),
    USER_NOT_EXISTED(1015, "Không tìm thấy người dùng!"),
    DEVICE_REJECTED(1016, "Thiết bị không hợp lệ!"),
    DEVICE_DISABLED(1017, "Thiết bị đã bị khóa!"),
    DEVICE_PENDING(1018, "Thiết bị trong trạng thái chờ admin phê duyệt!"),
    EMPTY_AREA(1019, "Không tìm thấy bất kỳ khu vực nào phù hợp!"),
    SNAPSHOT_NOT_FOUND(1020, "Chưa có dữ liệu tổng hợp mới nhất cho khu vực này!"),
    EMPTY_ACTIVE_USERS(1021, "Danh sách người dùng đang hoạt động trống!"),
    RESCUE_TEAM_EXISTED(1022, "Tên đội cứu hộ này đã tồn tại!"),
    RESCUE_TEAM_NOT_FOUND(1023, "Không tìm thấy tên đội cứu hộ phù hợp!"),
    INVALID_EXCEL_FILE(1024, "File excel không hợp lệ!"),
    RESCUER_NOT_IN_TEAM(1025, "Không thuộc đội cứu hộ hiện tại!"),
    USER_IS_NOT_RESCUER(1026, "Người dùng không phải là lực lượng cứu hộ"),
    RESCUE_GROUP_EXISTED(1027,"Nhóm cứu hộ đã tồn tại!"),
    RESCUE_GROUP_NOT_FOUND(1028,"Không tìm thấy nhóm cứu hộ!"),
    AREA_RISK_NOT_FOUND(1029,"Không tìm thấy dữ liệu tổng hợp mới nhất!"),
    CLIENT_DEVICE_REQUIRED(1030,"Không tìm thấy ID máy chủ!"),
    SODT_REQUIRED(1031,"Số điện thoại không được để trống!"),
    ACTIVE_SOS_ALREADY_EXISTS(1032,"Bạn đang có yêu cầu cứu hộ đang được xử lý!"),
    UNAUTHORIZED_UPDATE_SOS(1033,"Bạn không có quyền cập nhật yêu cầu cứu hộ!"),
    SOS_NOT_FOUND(1034,"Không tìm thấy yêu cầu cứu hộ!"),
    SOS_CANNOT_UPDATE(1035,"Yêu cầu cứu hộ không thể cập nhật!"),
    ACTIVED_SOS_NOT_FOUND(1036,"Danh sách yêu cầu trống!"),
    LIST_GROUP_NOT_FOUND(1037,"Danh sách nhóm cứu hộ thuộc đội này đang trống!"),
    LIST_TEAM_NOT_FOUND(1038,"Khu vực chưa có đội cứu hộ nào!"),
    ;

    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
