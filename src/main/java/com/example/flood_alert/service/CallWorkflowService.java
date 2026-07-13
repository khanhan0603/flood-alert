package com.example.flood_alert.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.UpdateCallResultRequest;
import com.example.flood_alert.dbo.response.CallResultResponse;
import com.example.flood_alert.dbo.response.CallTaskResponse;
import com.example.flood_alert.dbo.response.UpdateCallResultResponse;
import com.example.flood_alert.entity.CallLog;
import com.example.flood_alert.entity.CallTask;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.CallResult;
import com.example.flood_alert.enums.CallTargetType;
import com.example.flood_alert.enums.CallTaskStatus;
import com.example.flood_alert.enums.CallType;
import com.example.flood_alert.enums.DispatcherType;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.CallTaskMapper;
import com.example.flood_alert.repository.CallLogRepository;
import com.example.flood_alert.repository.CallTaskRepository;
import com.example.flood_alert.repository.SosRequestRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CallWorkflowService {
    Integer DEFAULT_TIMEOUT_SECONDS = 30;
    CallTaskRepository callTaskRepository;
    CallLogRepository callLogRepository;
    AuthenticationService authenticationService;
    SosRequestRepository sosRequestRepository;
    CallTaskMapper callTaskMapper;
    UserRepository userRepository;
    AlarmService alarmService;
    NotificationService notificationService;

    /**
     * Khởi tạo Call Workflow đầu tiên sau khi Hotline tạo SOS
     */
    public CallTask createInitialCallTask(SosRequest sosRequest) {

        User leader = sosRequest.getTeam().getLeader();

        if (leader == null) {
            throw new AppException(ErrorCode.TEAM_LEADER_NOT_FOUND);
        }

        CallTask callTask = CallTask.builder()
                .targetUser(leader)
                .targetType(CallTargetType.TEAM_LEADER)
                .retryCount(0)
                .timeoutSeconds(DEFAULT_TIMEOUT_SECONDS)
                .status(CallTaskStatus.CALLING_TEAM_LEADER)
                .sosRequest(sosRequest)
                .build();

        return callTaskRepository.save(callTask);
    }

    @Transactional
    public UpdateCallResultResponse updateCallResult(UUID callTaskId, UpdateCallResultRequest request) {

        CallTask callTask = callTaskRepository.findById(callTaskId)
                .orElseThrow(() -> new AppException(ErrorCode.CALL_TASK_NOT_FOUND));

        if (callTask.getStatus() == CallTaskStatus.SUCCESS) {
            throw new AppException(ErrorCode.CALL_TASK_FINISHED);
        }

        saveCallLog(callTask, request);

        switch (request.getCallResult()) {

            case ANSWERED -> handleAnswered(callTask);

            case NO_ANSWER, FAILED -> handleCallFailed(callTask);
        }

        return callTaskMapper.toUpdateCallResultResponse(callTaskRepository.save(callTask));
    }

    // Xử lý cuộc gọi thành công
    private void handleAnswered(CallTask callTask) {

        callTask.setStatus(CallTaskStatus.SUCCESS);

        SosRequest sos = callTask.getSosRequest();

        if (sos == null) {
            return;
        }

        switch (callTask.getTargetType()) {

            case TEAM_LEADER -> {
                sos.setDispatcherUser(callTask.getTargetUser());
                sos.setDispatcherType(DispatcherType.TEAM_LEADER);
            }

            case DEPUTY_LEADER -> {
                sos.setDispatcherUser(callTask.getTargetUser());
                sos.setDispatcherType(DispatcherType.DEPUTY_LEADER);
            }

            case PROVINCE_OPERATOR -> {
                sos.setDispatcherUser(callTask.getTargetUser());
                sos.setDispatcherType(DispatcherType.PROVINCE_OPERATOR);
            }

            default ->
                throw new AppException(ErrorCode.INVALID_CALL_TARGET);
        }

        sosRequestRepository.save(sos);
    }

    // Xử lý cuộc gọi không thành công
    private void handleCallFailed(CallTask callTask) {

        // Tăng số lần retry của target hiện tại
        callTask.setRetryCount(callTask.getRetryCount() + 1);

        // Chưa đủ 3 lần thì tiếp tục gọi cùng người
        if (callTask.getRetryCount() < 3) {
            return;
        }

        // Đủ 3 lần thì chuyển sang target tiếp theo
        switch (callTask.getTargetType()) {

            case TEAM_LEADER -> moveToDeputy(callTask);

            case DEPUTY_LEADER -> moveToFirstProvinceOperator(callTask);

            case PROVINCE_OPERATOR -> moveToNextProvinceOperator(callTask);

            default -> throw new AppException(ErrorCode.INVALID_CALL_TARGET);
        }
    }

    // Chuyển sang Deputy
    private void moveToDeputy(CallTask callTask) {

        RescueTeam team = callTask.getSosRequest().getTeam();

        User deputy = team.getDeputyLeader();

        if (deputy == null) {
            throw new AppException(ErrorCode.DEPUTY_LEADER_NOT_FOUND);
        }

        callTask.setTargetUser(deputy);
        callTask.setTargetType(CallTargetType.DEPUTY_LEADER);
        callTask.setRetryCount(0);
        callTask.setStatus(CallTaskStatus.CALLING_DEPUTY);
    }

    // Chuyển sang operator đầu tiên
    private void moveToFirstProvinceOperator(CallTask callTask) {

        UUID areaId = callTask.getSosRequest().getArea().getParent().getId();

        List<User> operators = userRepository.findByRoleAndArea_IdAndTrangthai(
                Role.PROVINCE_OPERATOR,
                areaId,
                Status.ACTIVE);

        if (operators.isEmpty()) {
            throw new AppException(ErrorCode.PROVINCE_OPERATOR_NOT_FOUND);
        }

        callTask.setTargetUser(operators.get(0));
        callTask.setTargetType(CallTargetType.PROVINCE_OPERATOR);
        callTask.setRetryCount(0);
        callTask.setStatus(CallTaskStatus.CALLING_PROVINCE);
    }

    // Chuyển sang operator tiếp theo
    private void moveToNextProvinceOperator(CallTask callTask) {

        UUID areaId = callTask.getSosRequest().getArea().getParent().getId();

        List<User> operators = userRepository.findByRoleAndArea_IdAndTrangthai(
                Role.PROVINCE_OPERATOR,
                areaId,
                Status.ACTIVE);

        if (operators.isEmpty()) {
            throw new AppException(ErrorCode.PROVINCE_OPERATOR_NOT_FOUND);
        }

        int currentIndex = -1;

        for (int i = 0; i < operators.size(); i++) {

            if (operators.get(i).getId().equals(callTask.getTargetUser().getId())) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            throw new AppException(ErrorCode.PROVINCE_OPERATOR_NOT_FOUND);
        }

        // Đã là Operator cuối cùng
        if (currentIndex == operators.size() - 1) {
            handleWorkflowFailed(callTask);

            return;
        }

        User nextOperator = operators.get(currentIndex + 1);

        callTask.setTargetUser(nextOperator);
        callTask.setTargetType(CallTargetType.PROVINCE_OPERATOR);
        callTask.setRetryCount(0);
        callTask.setStatus(CallTaskStatus.CALLING_PROVINCE);
    }

    /**
     * Toàn bộ Call Workflow thất bại.
     * Được gọi khi tất cả Province Operator đều không phản hồi.
     */
    private void handleWorkflowFailed(CallTask callTask) {

        // Kết thúc Call Workflow
        callTask.setStatus(CallTaskStatus.FAILED);

        SosRequest sos = callTask.getSosRequest();

        // 1. Tạo Alarm
        alarmService.createCallWorkflowFailedAlarm(callTask);

        // 2. Tạo Notification
        notificationService.createCallWorkflowFailedNotifications(sos);
    }

    // Lưu call log
    private void saveCallLog(
            CallTask callTask,
            UpdateCallResultRequest request) {

        User caller = authenticationService.getCurrentUser();

        CallLog callLog = CallLog.builder()
                .callerUser(caller)
                .receiverUser(callTask.getTargetUser())
                .phoneNumber(callTask.getTargetUser().getSodt())
                .callType(callTask.getSosRequest() != null
                        ? CallType.SOS
                        : CallType.SUPPORT_REQUEST)
                .callResult(request.getCallResult())
                .attempt(callTask.getRetryCount() + 1)
                .startedAt(request.getStartedAt())
                .endedAt(request.getEndedAt())
                .sosRequest(callTask.getSosRequest())
                .supportRequest(callTask.getSupportRequest())
                .build();

        callLogRepository.save(callLog);
    }

    // Lấy CallTask hiện tại theo sosId
    @Transactional(readOnly = true)
    public CallTaskResponse getBySosId(UUID sosId) {

        CallTask callTask = callTaskRepository.findBySosId(sosId)
                .orElseThrow(() -> new AppException(ErrorCode.CALL_TASK_NOT_FOUND));

        return callTaskMapper.toResponse(callTask);
    }

    // FE đang ở màn hình gọi điện và muốn refresh trạng thái của CallTask hiện tại.
    @Transactional(readOnly = true)
    public CallTaskResponse getById(UUID callTaskId) {

        CallTask callTask = callTaskRepository.findById(callTaskId)
                .orElseThrow(() -> new AppException(ErrorCode.CALL_TASK_NOT_FOUND));

        return callTaskMapper.toResponse(callTask);
    }

    // Danh sách status của call để FE chọn trong combo box
    public List<CallResultResponse> getCallResults() {

        return List.of(
                CallResultResponse.builder()
                        .value(CallResult.ANSWERED.name())
                        .label("Đã nhận điều phối")
                        .build(),

                CallResultResponse.builder()
                        .value(CallResult.NO_ANSWER.name())
                        .label("Không liên lạc được")
                        .build(),

                CallResultResponse.builder()
                        .value(CallResult.FAILED.name())
                        .label("Cuộc gọi lỗi")
                        .build());
    }
}
