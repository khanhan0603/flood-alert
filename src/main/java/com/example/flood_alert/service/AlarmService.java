package com.example.flood_alert.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.response.AlarmResponse;
import com.example.flood_alert.entity.Alarm;
import com.example.flood_alert.entity.CallTask;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.mapper.AlarmMapper;
import com.example.flood_alert.repository.AlarmRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AlarmService {

    AlarmRepository alarmRepository;
    AuthenticationService authenticationService;
    AlarmMapper alarmMapper;

    /**
     * Tạo Alarm khi toàn bộ Call Workflow thất bại.
     */
    public Alarm createCallWorkflowFailedAlarm(CallTask callTask) {

        SosRequest sos = callTask.getSosRequest();

        Alarm alarm = Alarm.builder()
                .title("Không có người nhận điều phối SOS")
                .message(buildAlarmMessage(sos))
                .callTask(callTask)
                .sosRequest(sos)
                .build();

        return alarmRepository.save(alarm);
    }

    /**
     * Sinh nội dung Alarm.
     */
    private String buildAlarmMessage(SosRequest sos) {

        return String.format(
                "SOS %s tại %s (Ưu tiên: %s) không có Team Leader, Deputy Leader hoặc Province Operator nào xác nhận nhận điều phối.",
                sos.getTrackingCode(),
                sos.getArea().getTenkhuvuc(),
                sos.getPriority());
    }

    // Danh sách alarm
    @Transactional(readOnly = true)
    public Page<AlarmResponse> getMyTeamAlarms(Pageable pageable) {

        User operator = authenticationService.getCurrentUser();

        UUID teamId = operator.getTeam().getId();

        log.info("Operator = {}", operator.getHoten());
        log.info("TeamId = {}", teamId);

        return alarmRepository
                .findAlarmByTeamId(teamId, pageable)
                .map(alarmMapper::toResponse);
    }
}