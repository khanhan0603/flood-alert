package com.example.flood_alert.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),

    INVALID_KEY(1005, "Invalid message key"),

    INVALID_EXCEL_FILE(1024, "File excel không hợp lệ!"),

    INVALID_PASSWORD(1002, "Mật khẩu phải ít nhất 6 ký tự!"),
    INVALID_EMAIL(1001, "Email không hợp lệ! Dạng email@gmail.com!"),
    INVALID_PHONE(1006, "Số điện thoại không hợp lệ! Dạng 0xxxxxxxxx!"),
    EMAIL_EXISTED(1007, "Email này đã được đăng ký! Vui lòng dùng email khác!"),
    PHONE_EXISTED(1008, "Số điện thoại này đã được đăng ký! Vui số dùng số khác!"),
    SODT_REQUIRED(1031, "Số điện thoại không được để trống!"),
    NO_PERMISSION(1039, "Không có quyền hạn trong thao tác này!"),
    LOCATION_REQUIRED(1062, "Địa chỉ không được để trống!"),
    ADDRESS_TOO_LONG(2007, "Địa chỉ quá dài! Tối đa 500 ký tự!"),

    INVALID_RESET_TOKEN(1087, "Token đặt lại mật khẩu không hợp lệ!"),
    RESET_TOKEN_EXPIRED(1088, "Token đặt lại mật khẩu đã hết hạn!"),
    RESET_TOKEN_USED(1089, "Token đặt lại mật khẩu đã được sử dụng!"),
    USER_ALREADY_INACTIVE(1087, "Tài khoản đã bị khóa!"),
    ACCOUNT_ALREADY_ACTIVE(1088, "Tài khoản đã ở trạng thái hoạt động!"),
    ACCOUNT_LOCKED_NOT_FOUND(1089, "Không tìm thấy tài khoản bị khóa!"),
    INVALID_UNLOCK_OTP(1092, "Mã xác thực không hợp lệ!"),
    UNLOCK_OTP_EXPIRED(1090, "Mã xác thực đã hết hạn!"),
    UNLOCK_OTP_USED(1091, "Mã xác thực đã được sử dụng!"),
    WRONG_PASSWORD(2001, "Mật khẩu hiện tại không chính xác!"),
    NEW_PASSWORD_MUST_BE_DIFFERENT(2002, "Mật khẩu mới phải khác mật khẩu hiện tại!"),
    ACCOUNT_LOCKED(2005, "Tài khoản đã bị khóa!"),

    USER_NOT_EXISTED(1015, "Không tìm thấy người dùng!"),
    EMPTY_ACTIVE_USERS(1021, "Danh sách người dùng đang hoạt động trống!"),

    USER_IS_NOT_CITIZEN(2000, "Người dùng không phải người dân nên không dùng chức năng này!"),

    LOGIN_INFO_EXISTED(1009, "Thông tin đăng nhập không chính xác"),
    UNAUTHENTICATED(1010, "Xác thực không hợp lệ!!!"),

    EMPTY_AREA_BY_PARENT_ID(1004, "Không tìm thấy danh sách phường/xã hợp lệ!"),
    EMPTY_POLYGIN_BY_ID(1003, "Không tìm thấy ranh giới hợp lệ!"),
    AREA_NOT_FOUND(1011, "Không tìm thấy khu vực phù hợp!"),
    EMPTY_AREA(1019, "Không tìm thấy bất kỳ khu vực nào phù hợp!"),

    DEVICE_CODE_EXISTED(1012, "Mã thiết bị này đã tồn tại!"),
    DEVICE_NOT_FOUND(1013, "Không tìm thấy thiết bị!"),
    DEVICE_ALREADY_PROCESSED(1014, "Thiết bị này đã được xử lý!"),
    DEVICE_REJECTED(1016, "Thiết bị không hợp lệ!"),
    DEVICE_DISABLED(1017, "Thiết bị đã bị khóa!"),
    DEVICE_PENDING(1018, "Thiết bị trong trạng thái chờ admin phê duyệt!"),

    SNAPSHOT_NOT_FOUND(1020, "Chưa có dữ liệu đánh giá rủi ro mới nhất cho khu vực này!"),

    AREA_RISK_NOT_FOUND(1029, "Không tìm thấy dữ liệu tổng hợp mực nước mới nhất!"),

    PROVINCE_OPERATOR_NOT_FOUND(1080, "Không tìm thấy danh sách lực lượng điều phối cấp tỉnh!"),
    USER_IS_NOT_PROVINCE_OPERATOR(1099, "Người dùng không phải điều phối viên cấp tỉnh!"),

    RESCUE_TEAM_EXISTED(1022, "Tên đội cứu hộ này đã tồn tại!"),
    RESCUE_TEAM_NOT_FOUND(1023, "Không tìm thấy tên đội cứu hộ phù hợp!"),
    RESCUER_NOT_IN_TEAM(1025, "Không thuộc đội cứu hộ hiện tại!"),
    USER_IS_NOT_RESCUER(1026, "Người dùng không phải là lực lượng cứu hộ"),
    RESCUE_GROUP_EXISTED(1027, "Nhóm cứu hộ đã tồn tại!"),
    RESCUE_GROUP_NOT_FOUND(1028, "Không tìm thấy nhóm cứu hộ!"),
    LIST_TEAM_NOT_FOUND(1038, "Khu vực chưa có đội cứu hộ nào!"),
    USER_ALREADY_IS_TEAM_LEADER_OR_DEPUTY(1074, "Người dùng đã là đội trưởng hoặc phó đội trưởng của đội khác!"),
    LEADER_AND_DEPUTY_CANNOT_BE_THE_SAME(1075, "Người này có thể đã là đội trưởng!"),
    TEAM_LEADER_NOT_FOUND(1076, "Không tìm thấy đội trưởng!"),
    DEPUTY_LEADER_NOT_FOUND(1079, "Không tìm thấy phó đội trưởng!"),

    LIST_GROUP_NOT_FOUND(1037, "Danh sách nhóm cứu hộ thuộc đội này đang trống!"),
    GROUP_NOT_FOUND(1043, "Không tìm thấy nhóm cứu hộ!"),
    GROUP_MEMBER_NOT_FOUND(1051, "Không tìm thấy người dùng trong nhóm!"),
    GROUP_LEADER_CANNOT_REMOVE(1052, "Không được loại trưởng nhóm ra khỏi nhóm!"),
    GROUP_LEADER_CANNOT_DELETE(1053, "Không được xóa trưởng nhóm!"),
    GROUP_MEMBER_LIMIT_EXCEEDED(2003, "Số lượng thành viên đã vượt giới hạn của nhóm!"),
    GROUP_MEMBER_NOT_ENOUGH(2004, "Số lượng thành viên trong nhóm không đủ để thực hiện nhiệm vụ"),

    CLIENT_DEVICE_REQUIRED(1030, "Không tìm thấy ID máy chủ!"),
    ACTIVE_SOS_ALREADY_EXISTS(1032, "Bạn đang có yêu cầu cứu hộ đang được xử lý!"),
    UNAUTHORIZED_UPDATE_SOS(1033, "Bạn không có quyền cập nhật yêu cầu cứu hộ!"),
    SOS_NOT_FOUND(1034, "Không tìm thấy yêu cầu cứu hộ!"),
    SOS_CANNOT_UPDATE(1035, "Yêu cầu cứu hộ không thể cập nhật!"),
    ACTIVED_SOS_NOT_FOUND(1036, "Danh sách yêu cầu trống!"),
    SOS_ALREADY_CLAIMED(1085, "Yêu cầu cứu hộ đã có người nhận điều phối!"),
    VICTIM_COUNT_REQUIRED(1100, "Số lượng nạn nhân không được để trống!"),
    VICTIM_COUNT_INVALID(1101, "Số lượng nạn nhân phải lớn hơn hoặc bằng 1!"),
    LAT_REQUIRED(1102, "Vĩ độ không được để trống!"),
    LAT_INVALID(1103, "Vĩ độ phải nằm trong khoảng từ -90 đến 90!"),
    LON_REQUIRED(1104, "Kinh độ không được để trống!"),
    LON_INVALID(1105, "Kinh độ phải nằm trong khoảng từ -180 đến 180!"),
    DESCRIPTION_TOO_LONG(2011,"Đoạn mô tả quá dài! Không vượt quá 1000 ký tự!"),
    SOS_REQUIRED(2010,"Không được bỏ trống mã yêu cầu cứu hộ!"),

    ASSIGNMENT_NOT_FOUND(1040, "Không tìm thấy nhiệm vụ!"),
    GROUP_NOT_AVAILABLE(1042, "Nhóm cứu hộ hiện tại không sẵn sàng nhận nhiệm vụ!"),
    INVALID_SUPPORT_ITEM_STATUS(1057, "Trạng thái của chi tiết yêu cầu không hợp lệ!"),
    SUPPORT_REQUEST_NOT_FULLY_REVIEWED(1058, "Cần xử lý hết tất cả các chi tiết yêu cầu hỗ trợ!"),
    SUPPORT_GROUP_ALREADY_ASSIGNED_ENOUGH(1059, "Đã phân công đủ số nhóm cứu hộ!"),
    ASSIGNED_TEAM_REQUIRED(1056, "Bắt buộc phải chọn team chỉ định với yêu cầu!"),
    INVALID_ASSIGNMENT_STATUS(1070, "Trạng thái nhiệm vụ không hợp lệ!"),
    INVALID_GROUP_STATUS(1071, "Trạng thái nhóm cứu hộ không hợp lệ!"),
    GROUP_LEADER_NOT_FOUND(1083, "Không tìm thấy nhóm trưởng phụ trách nhiệm vụ này!"),

    SUPPORT_REQUEST_NOT_FOUND(1041, "Không tìm thấy yêu cầu hỗ trợ phù hợp!"),
    SUPPORT_REQUEST_ALREADY_EXISTS(1044, "Đơn yêu cầu hỗ trợ đã tồn tại!"),
    SUPPORT_REQUEST_ALREADY_REVIEWED(1045, "Yêu cầu hỗ trợ này đã được phê duyệt!"),
    SUPPORT_REQUEST_NOT_APPROVED(1046, "Yêu cầu hỗ trợ chưa được phê duyệt!"),
    SOS_ALREADY_ASSIGNED(1048, "Yêu cầu cứu hộ đã được giao!"),
    INVALID_SUPPORT_REQUEST_STATUS(1049, "Trạng thái yêu cầu hỗ trợ không hợp lệ!"),
    SOS_CANNOT_CANCEL(1050, "Yêu cầu cứu hộ không thể hủy!"),
    DUPLICATE_SUPPORT_TYPE(1054, "Trùng loại yêu cầu hỗ trợ!"),
    SUPPORT_REQUEST_ITEM_NOT_FOUND(1055, "Không tìm thấy chi tiết yêu cầu hỗ trợ!"),
    GROUP_NOT_SUPPORT_TYPE(1060, "Loại hỗ trợ không đúng!"),
    INVALID_SUPPORT_TYPE(1073, "Loại hỗ trợ không hợp lệ!"),
    SUPPORT_REQUEST_ITEM_REQUIRED(1072, "Không được để chi tiết yêu cầu hỗ trợ trống!"),
    SUPPORT_REQUEST_ALREADY_CLAIMED(1084, "Yêu cầu hỗ trợ đã có người nhận điều phối!"),
    REASON_REQUIRED(2008,"Lý do cần hỗ trợ không được để trống!"),
    REASON_TOO_LONG(2009,"Lý do hỗ trợ quá dài! Không được vượt quá 1000 ký tự!"),
    SUPPORT_TYPE_REQUIRED(2011,"Không được để trống loại nhóm cần hỗ trợ!"),

    INVALID_HOTLINE_GROUP_CAPABILITY(1061, "Nhóm Hotline không được có năng lực cứu hộ!"),
    CALL_EVENT_NOT_FOUND(1063, "Không tìm thấy cuộc gọi hoặc cuộc gọi đã được xử lý!"),
    INVALID_CALL_EVENT_STATUS(1064, "Trang thái cuộc gọi không hợp lệ!"),
    CALL_TASK_NOT_FOUND(1077, "Không tìm thấy tác vụ cuộc gọi!"),
    CALL_TASK_FINISHED(1078, "Tác vụ này đã hoàn thành!"),
    INVALID_CALL_TARGET(1081, "Đối tượng gọi không hợp lệ!"),

    GENERATE_TRACKING_CODE_FAILED(1065, "Không thể tạo mã tra cứu. Vui lòng thử lại!"),
    TRACKING_CODE_REQUIRED(1066, "Mã tra cứu không được để trống!"),
    SEARCH_CONDITION_REQUIRED(1067, "Vui lòng nhập đủ các điều kiện!"),
    INVALID_DATE_RANGE(2006, "Ngày kết thúc không được nhỏ hơn ngày bắt đầu!"),

    PREDICTION_JOB_NOT_FOUND(1068, "Không tìm thấy phiên chạy dự báo lũ lụt theo dữ liệu thời tiết!"),

    NOTIFICATION_NOT_FOUND(1082, "Không tìm thấy thông báo!"),

    FLOOD_ALERT_NOT_FOUND(1086, "Không tìm thấy thông tin cảnh báo lũ lụt!"),
    ;

    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
