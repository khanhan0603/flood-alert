package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.CreateHotlineSosRequest;
import com.example.flood_alert.dbo.request.EmergencyContactRequest;
import com.example.flood_alert.dbo.request.SearchHotlineSosRequest;
import com.example.flood_alert.dbo.response.CallEventResponse;
import com.example.flood_alert.dbo.response.EmergencyContactResponse;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.dbo.response.StatusOptionResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.EmergencyCallEvent;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.CallEventStatus;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.LocationSource;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.SosSource;
import com.example.flood_alert.enums.Status;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.EmergencyCallEventMapper;
import com.example.flood_alert.mapper.SosRequestMapper;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.EmergencyCallEventRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
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
public class HotlineService {

        EmergencyCallEventRepository emergencyCallEventRepository;

        AuthenticationService authenticationService;

        SosRequestRepository sosRequestRepository;

        UserRepository userRepository;

        AreaRepository areaRepository;

        AreaRiskSnapshotRepository areaRiskSnapshotRepository;

        RescueTeamRepository rescueTeamRepository;

        EnvironmentRiskEvaluator environmentRiskEvaluator;

        SosPriorityCalculator sosPriorityCalculator;

        PriorityReasonGenerator priorityReasonGenerator;

        SosRequestMapper sosRequestMapper;

        NotificationService notificationService;

        List<StatusSOS> ACTIVE_STATUSES = List.of(
                        StatusSOS.PENDING,
                        StatusSOS.PROCESSING);

        EmergencyCallEventMapper emergencyCallEventMapper;
        TrackingCodeGenerator trackingCodeGenerator;

        // Lấy số điện thoại liên hệ của đội gần người dân nhất
        @Transactional
        public EmergencyContactResponse createEmergencyCall(
                        EmergencyContactRequest request) {

                UUID areaId = areaRepository.findAreaIdByLatLon(
                                request.getLat(),
                                request.getLon());

                if (areaId == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                RescueTeam team = rescueTeamRepository
                                .findByArea_Id(areaId)
                                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND));

                EmergencyCallEvent callEvent = EmergencyCallEvent.builder()
                                .team(team)
                                .callerLat(request.getLat())
                                .callerLon(request.getLon())
                                .callerPhoneNumber(request.getCallerPhoneNumber())
                                .status(CallEventStatus.PENDING_MATCH)
                                .createdAt(LocalDateTime.now())
                                .build();

                callEvent = emergencyCallEventRepository.save(callEvent);

                return EmergencyContactResponse.builder()
                                .callEventId(callEvent.getId())
                                .teamId(team.getId())
                                .teamName(team.getName())
                                .emergencyPhone(team.getEmergencyPhone())
                                .build();
        }

        @Transactional
        public SosResponse createHotlineSos(CreateHotlineSosRequest request) {

                validateCreateHotlineSosRequest(request);

                // Operator tạo SOS
                User operator = authenticationService.getCurrentUser();

                EmergencyCallEvent callEvent = null;

                String sodt;
                BigDecimal lat;
                BigDecimal lon;

                // =====================
                // Lấy thông tin từ Call Event hoặc nhập tay
                // =====================

                if (request.getCallEventId() != null) {

                        callEvent = emergencyCallEventRepository
                                        .findByIdAndStatus(
                                                        request.getCallEventId(),
                                                        CallEventStatus.PENDING_MATCH)
                                        .orElseThrow(() -> new AppException(
                                                        ErrorCode.CALL_EVENT_NOT_FOUND));

                        sodt = callEvent.getCallerPhoneNumber();
                        lat = callEvent.getCallerLat();
                        lon = callEvent.getCallerLon();

                } else {

                        sodt = request.getSodt();
                        lat = request.getLat();
                        lon = request.getLon();
                }

                // =====================
                // Tìm người dân theo số điện thoại
                // =====================

                User citizen = userRepository
                                .findBySodtAndStatus(sodt, Status.ACTIVE)
                                .orElse(null);

                boolean anonymous = citizen == null;

                // =====================
                // Kiểm tra SOS đang hoạt động
                // =====================

                Optional<SosRequest> activeSos;

                if (citizen != null) {

                        activeSos = sosRequestRepository.findFirstByUserIdAndStatusIn(
                                        citizen.getId(),
                                        ACTIVE_STATUSES);

                } else {

                        activeSos = sosRequestRepository.findFirstBySodtAndStatusIn(
                                        sodt,
                                        ACTIVE_STATUSES);
                }

                if (activeSos.isPresent()) {

                        SosResponse response = sosRequestMapper.toResponse(activeSos.get());

                        response.setAlreadyExists(true);

                        return response;
                }

                // =====================
                // Xác định Area
                // =====================

                UUID areaId = areaRepository.findAreaIdByLatLon(lat, lon);

                if (areaId == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                Area area = areaRepository.findById(areaId)
                                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

                // =====================
                // Tìm Rescue Team
                // =====================

                RescueTeam team;

                if (callEvent != null) {

                        // Dùng luôn Team đã gán khi tạo EmergencyCallEvent
                        team = callEvent.getTeam();

                } else {

                        // Operator nhập tay
                        team = rescueTeamRepository
                                        .findFirstByArea_IdOrderByCreatedAtAsc(areaId)
                                        .orElseThrow(() -> new AppException(
                                                        ErrorCode.RESCUE_TEAM_NOT_FOUND));
                }

                // =====================
                // Snapshot
                // =====================

                AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                                .findLatestSnapshotByAreaId(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_RISK_NOT_FOUND));

                // =====================
                // Environment Risk
                // =====================

                EnvironmentRisk environmentRisk = environmentRiskEvaluator.evaluate(snapshot);

                // =====================
                // Priority
                // =====================

                Priority priority = sosPriorityCalculator.calculate(
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);

                // =====================
                // Priority Reason
                // =====================

                String priorityReason = priorityReasonGenerator.generate(
                                priority,
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);
                // Build SosRequest
                // 6. Tạo SOS
                SosRequest sos = SosRequest.builder()

                                // Người cần cứu hộ
                                .user(citizen)

                                // Operator tạo SOS
                                .createdByUser(operator)

                                .area(area)
                                .team(team)

                                .anonymous(anonymous)

                                .sodt(sodt)

                                .lat(lat)
                                .lon(lon)

                                // Hotline chỉ nhập địa chỉ thủ công
                                .diachi(request.getRawAddressText())

                                .victimCount(request.getVictimCount())

                                .injured(request.getInjured())
                                .trapped(request.getTrapped())
                                .vulnerable(request.getVulnerable())

                                .mota(request.getMota())

                                .priority(priority)
                                .priorityReason(priorityReason)

                                .environmentRisk(environmentRisk)

                                .snapshotWaterRise(
                                                snapshot.getWaterRiseRatePerMinute())

                                .snapshotDangerRatio(
                                                snapshot.getDangerRatio())

                                .snapshotPredictionProbability(
                                                snapshot.getPredictionProbability())

                                .locationConfirmed(true)
                                .lastLocationUpdate(LocalDateTime.now())

                                .status(StatusSOS.PENDING)

                                .sosSource(SosSource.HOTLINE_OPERATOR)

                                .locationSource(
                                                callEvent != null
                                                                ? LocationSource.GPS_FROM_CALL_EVENT
                                                                : LocationSource.MANUAL_ADDRESS)

                                .linkedCallEvent(callEvent)

                                .build();

                // Lưu
                sos = trackingCodeGenerator.save(sos);

                // Nếu tạo từ emergencyCallEvent
                if (callEvent != null) {

                        callEvent.setStatus(CallEventStatus.MATCHED);

                        callEvent.setConvertedToSos(sos);

                        emergencyCallEventRepository.save(callEvent);
                }

                // Gửi thông báo
                if (team.getLeader() != null) {

                        notificationService.sendNewSosNotification(
                                        team.getLeader(),
                                        sos);
                }

                // response
                SosResponse response = sosRequestMapper.toResponse(sos);

                response.setAlreadyExists(false);

                return response;
        }

        private void validateCreateHotlineSosRequest(
                        CreateHotlineSosRequest request) {

                // Operator nhập tay
                if (request.getCallEventId() == null) {

                        if (request.getSodt() == null
                                        || request.getSodt().isBlank()) {

                                throw new AppException(ErrorCode.SODT_REQUIRED);
                        }

                        if (request.getLat() == null
                                        || request.getLon() == null) {

                                throw new AppException(ErrorCode.LOCATION_REQUIRED);
                        }
                }
        }

        // Danh sách cuộc gọi chờ Operator xử lý chỉ xem cuộc gọi đang chờ.
        @Transactional(readOnly = true)
        public Page<CallEventResponse> getPendingCallEvents(
                        Pageable pageable) {

                return emergencyCallEventRepository
                                .findByStatusOrderByCreatedAtAsc(
                                                CallEventStatus.PENDING_MATCH,
                                                pageable)
                                .map(emergencyCallEventMapper::toResponse);
        }

        // Chi tiết cuộc gọi
        @Transactional(readOnly = true)
        public CallEventResponse getCallEvent(UUID id) {

                EmergencyCallEvent callEvent = emergencyCallEventRepository
                                .findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.CALL_EVENT_NOT_FOUND));

                return emergencyCallEventMapper.toResponse(callEvent);
        }

        // Danh sách chỉ xem lịch sử (MATCHED, STALE).
        @Transactional(readOnly = true)
        public Page<CallEventResponse> getHistory(
                        CallEventStatus status,
                        Pageable pageable) {
                if (status == CallEventStatus.PENDING_MATCH) {
                        throw new AppException(ErrorCode.INVALID_CALL_EVENT_STATUS);
                }

                return emergencyCallEventRepository
                                .findByStatusOrderByCreatedAtDesc(
                                                status,
                                                pageable)
                                .map(emergencyCallEventMapper::toResponse);
        }

        // Tra cứu cho hotline
        @Transactional(readOnly = true)
        public Page<SosResponse> searchHotlineSos(
                        SearchHotlineSosRequest request,
                        Pageable pageable) {

                String keyword = request.getKeyword();

                if (keyword != null) {
                        keyword = keyword.trim();

                        if (keyword.isBlank()) {
                                keyword = null;
                        }
                }

                return sosRequestRepository
                                .searchHotlineSos(
                                                keyword,
                                                request.getStatus(),
                                                pageable)
                                .map(sosRequestMapper::toResponse);
        }

        // Combo box chọn status
        @Transactional(readOnly = true)
        public List<StatusOptionResponse> getStatusOptions() {

                return Arrays.stream(StatusSOS.values())
                                .map(status -> StatusOptionResponse.builder()
                                                .value(status.name())
                                                .label(getStatusLabel(status))
                                                .build())
                                .toList();
        }

        private String getStatusLabel(StatusSOS status) {

                return switch (status) {

                        case PENDING -> "Chờ phân công";

                        case ASSIGNED -> "Đã phân công";

                        case PROCESSING -> "Đang cứu hộ";

                        case DONE -> "Hoàn thành";

                        case CANCELED -> "Đã hủy";
                };
        }

        // Danh sách các sos do hotline nhập thủ công
        @Transactional(readOnly = true)
        public Page<SosResponse> getManualHotlineSos(
                        Pageable pageable) {

                return sosRequestRepository
                                .findManualHotlineSos(pageable)
                                .map(sosRequestMapper::toResponse);
        }
}