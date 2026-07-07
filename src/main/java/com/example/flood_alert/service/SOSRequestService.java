package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.AnonymousSosListRequest;
import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.dbo.request.UpdateAnonymousSosRequest;
import com.example.flood_alert.dbo.request.UpdateSosRequest;
import com.example.flood_alert.dbo.response.CitizenAssignmentResponse;
import com.example.flood_alert.dbo.response.CitizenSosDetailResponse;
import com.example.flood_alert.dbo.response.SosAssignmentResponse;
import com.example.flood_alert.dbo.response.SosDetailResponse;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.dbo.response.TeamDashboardResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.LocationSource;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.SosSource;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.SosAssignmentMapper;
import com.example.flood_alert.mapper.SosRequestMapper;
import com.example.flood_alert.mapper.SupportRequestMapper;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SosAssignmentRepository;
import com.example.flood_alert.repository.SosRequestRepository;
import com.example.flood_alert.repository.SupportRequestRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SOSRequestService {
        AreaRepository areaRepository;

        AreaRiskSnapshotRepository areaRiskSnapshotRepository;

        SosRequestRepository sosRequestRepository;

        RescueTeamRepository rescueTeamRepository;

        SosAssignmentRepository sosAssignmentRepository;

        SupportRequestRepository supportRequestRepository;

        EnvironmentRiskEvaluator environmentRiskEvaluator;

        SosPriorityCalculator sosPriorityCalculator;

        PriorityReasonGenerator priorityReasonGenerator;

        SosRequestMapper sosRequestMapper;

        SosAssignmentMapper sosAssignmentMapper;

        SupportRequestMapper supportRequestMapper;
        AuthenticationService authenticationService;
        NotificationService notificationService;
        TrackingCodeGenerator trackingCodeGenerator;
        List<StatusSOS> ACTIVE_STATUSES = List.of(
                        StatusSOS.PENDING,
                        StatusSOS.PROCESSING);

        @Transactional
        @CacheEvict(value = "team-dashboard", allEntries = true)
        public SosResponse create(CreateSosRequest request, HttpServletRequest httpRequest) {

                User currentUser = authenticationService.getCurrentUserOrNull();

                boolean anonymous = currentUser == null;

                // log.info(
                // "SOS creator type: {}",
                // anonymous
                // ? "ANONYMOUS"
                // : "USER");

                String sodt;

                if (anonymous) {
                        sodt = request.getSodt();
                } else {
                        sodt = currentUser.getSodt();
                }
                // Nếu chưa đăng nhập, check có SDT và ClientId chưa
                if (anonymous) {

                        if (request.getSodt() == null
                                        || request.getSodt().isBlank()) {

                                throw new AppException(
                                                ErrorCode.SODT_REQUIRED);
                        }

                        if (request.getClientDeviceId() == null
                                        || request.getClientDeviceId().isBlank()) {

                                throw new AppException(
                                                ErrorCode.CLIENT_DEVICE_REQUIRED);
                        }
                }

                // Lấy IP
                String ipAddress = httpRequest.getHeader("X-Forwarded-For");

                if (ipAddress == null || ipAddress.isBlank()) {
                        ipAddress = httpRequest.getRemoteAddr();
                }

                // Check người dân đã từng gửi sos chưa
                Optional<SosRequest> activeSos;

                if (anonymous) {

                        activeSos = sosRequestRepository
                                        .findFirstBySodtAndClientDeviceIdAndStatusIn(
                                                        sodt,
                                                        request.getClientDeviceId(),
                                                        ACTIVE_STATUSES);

                } else {

                        activeSos = sosRequestRepository
                                        .findFirstByUserIdAndStatusIn(
                                                        currentUser.getId(),
                                                        ACTIVE_STATUSES);
                }

                if (activeSos.isPresent()) {
                        SosResponse response = sosRequestMapper.toResponse(
                                        activeSos.get());

                        response.setAlreadyExists(true);

                        return response;
                }

                // 1. Xác định khu vực từ tọa độ
                UUID areaId = areaRepository.findAreaIdByLatLon(request.getLat(), request.getLon());

                if (areaId == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                Area area = areaRepository.findById(areaId)
                                .orElseThrow(() -> new AppException(ErrorCode.AREA_NOT_FOUND));

                // Lấy team theo khu vực
                RescueTeam team = rescueTeamRepository.findFirstByArea_IdOrderByCreatedAtAsc(areaId)
                                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND));

                // 2. Lấy snapshot mới nhất của khu vực
                AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                                .findLatestSnapshotByAreaId(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_RISK_NOT_FOUND));

                // 3. Tính EnvironmentRisk
                EnvironmentRisk environmentRisk = environmentRiskEvaluator.evaluate(snapshot);

                // 4. Tính Priority
                Priority priority = sosPriorityCalculator.calculate(
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);

                // 5. Sinh PriorityReason
                String priorityReason = priorityReasonGenerator.generate(
                                priority,
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);

                // 6. Tạo SOS
                SosRequest sos = SosRequest.builder()
                                .user(currentUser)
                                .area(area)

                                .anonymous(anonymous)

                                .sodt(sodt)

                                .clientDeviceId(
                                                anonymous
                                                                ? request.getClientDeviceId()
                                                                : null)

                                .ipAddress(ipAddress)

                                .diachi(request.getDiachi())

                                .victimCount(request.getVictimCount())

                                .lat(request.getLat())
                                .lon(request.getLon())
                                .diachi(request.getDiachi())

                                .accuracy(request.getAccuracy())

                                .injured(request.getInjured())
                                .trapped(request.getTrapped())
                                .vulnerable(request.getVulnerable())

                                .mota(request.getMota())

                                .priority(priority)

                                .priorityReason(
                                                priorityReason)

                                .environmentRisk(
                                                environmentRisk)

                                .snapshotWaterRise(
                                                snapshot.getWaterRiseRatePerMinute())

                                .snapshotDangerRatio(
                                                snapshot.getDangerRatio())

                                .snapshotPredictionProbability(
                                                snapshot.getPredictionProbability())

                                .locationConfirmed(false)

                                .status(StatusSOS.PENDING)

                                .team(team)

                                .createdByUser(currentUser)

                                .sosSource(SosSource.DIRECT)

                                .locationSource(LocationSource.MANUAL_ADDRESS)

                                .build();

                sos = trackingCodeGenerator.save(sos);

                // Gửi thông báo cho Team Leader của Team phụ trách
                if (team.getLeader() != null) {

                        notificationService.sendNewSosNotification(
                                        team.getLeader(),
                                        sos);
                }

                // 8. Response
                SosResponse response = sosRequestMapper.toResponse(sos);

                response.setAlreadyExists(false);

                return response;
        }

        @Transactional
        public SosResponse update(
                        UUID sosId,
                        UpdateSosRequest request,
                        HttpServletRequest httpRequest) {

                User currentUser = authenticationService.getCurrentUser();

                boolean anonymous = currentUser == null;

                SosRequest sos;

                if (anonymous) {

                        throw new AppException(ErrorCode.UNAUTHORIZED_UPDATE_SOS);

                } else {

                        sos = sosRequestRepository
                                        .findByIdAndUserId(sosId, currentUser.getId())
                                        .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));
                }

                switch (sos.getStatus()) {

                        case PENDING -> updatePendingSos(sos, request);

                        case ASSIGNED, PROCESSING -> updateAssignedOrProcessingSos(
                                        sos,
                                        request);

                        case DONE, CANCELED -> throw new AppException(
                                        ErrorCode.SOS_CANNOT_UPDATE);
                }

                sos = sosRequestRepository.save(sos);

                SosResponse response = sosRequestMapper.toResponse(sos);

                response.setAlreadyExists(false);

                return response;
        }

        @Transactional
        public SosResponse updateAnonymous(UUID sosId,
                        UpdateAnonymousSosRequest request,
                        HttpServletRequest httpRequest) {
                SosRequest sos = sosRequestRepository
                                .findByIdAndSodtAndClientDeviceId(
                                                sosId,
                                                request.getSodt(),
                                                request.getClientDeviceId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.UNAUTHORIZED_UPDATE_SOS));
                switch (sos.getStatus()) {

                        case PENDING -> updatePendingAnonymousSos(sos, request);

                        case ASSIGNED, PROCESSING -> updateAssignedOrProcessingAnonymousSos(sos, request);

                        case DONE, CANCELED -> throw new AppException(
                                        ErrorCode.SOS_CANNOT_UPDATE);
                }

                sos = sosRequestRepository.save(sos);

                SosResponse response = sosRequestMapper.toResponse(sos);

                response.setAlreadyExists(false);

                return response;
        }

        private void updatePendingSos(
                        SosRequest sos,
                        UpdateSosRequest request) {

                sos.setVictimCount(request.getVictimCount());

                sos.setLat(request.getLat());

                sos.setLon(request.getLon());

                sos.setDiachi(request.getDiachi());

                sos.setAccuracy(request.getAccuracy());

                sos.setInjured(request.getInjured());

                sos.setTrapped(request.getTrapped());

                sos.setVulnerable(request.getVulnerable());

                sos.setMota(request.getMota());

                sos.setLastLocationUpdate(LocalDateTime.now());

                // =====================
                // Xác định lại khu vực
                // =====================

                UUID areaId = areaRepository.findAreaIdByLatLon(
                                request.getLat(),
                                request.getLon());

                if (areaId == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                Area area = areaRepository.findById(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_NOT_FOUND));

                sos.setArea(area);

                // =====================
                // Xác định lại Team
                // =====================

                RescueTeam team = rescueTeamRepository
                                .findFirstByArea_IdOrderByCreatedAtAsc(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_TEAM_NOT_FOUND));

                sos.setTeam(team);

                // =====================
                // Snapshot
                // =====================

                AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                                .findLatestSnapshotByAreaId(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_RISK_NOT_FOUND));

                EnvironmentRisk environmentRisk = environmentRiskEvaluator.evaluate(snapshot);

                Priority priority = sosPriorityCalculator.calculate(
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);

                String priorityReason = priorityReasonGenerator.generate(
                                priority,
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);

                sos.setEnvironmentRisk(environmentRisk);

                sos.setPriority(priority);

                sos.setPriorityReason(priorityReason);

                sos.setSnapshotWaterRise(
                                snapshot.getWaterRiseRatePerMinute());

                sos.setSnapshotDangerRatio(
                                snapshot.getDangerRatio());

                sos.setSnapshotPredictionProbability(
                                snapshot.getPredictionProbability());
        }

        private void updatePendingAnonymousSos(
                        SosRequest sos,
                        UpdateAnonymousSosRequest request) {

                sos.setVictimCount(request.getVictimCount());

                sos.setLat(request.getLat());

                sos.setLon(request.getLon());

                sos.setDiachi(request.getDiachi());

                sos.setAccuracy(request.getAccuracy());

                sos.setInjured(request.getInjured());

                sos.setTrapped(request.getTrapped());

                sos.setVulnerable(request.getVulnerable());

                sos.setMota(request.getMota());

                sos.setLastLocationUpdate(LocalDateTime.now());

                // =====================
                // Xác định lại khu vực
                // =====================

                UUID areaId = areaRepository.findAreaIdByLatLon(
                                request.getLat(),
                                request.getLon());

                if (areaId == null) {
                        throw new AppException(ErrorCode.AREA_NOT_FOUND);
                }

                Area area = areaRepository.findById(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_NOT_FOUND));

                sos.setArea(area);

                // =====================
                // Xác định lại Team
                // =====================

                RescueTeam team = rescueTeamRepository
                                .findFirstByArea_IdOrderByCreatedAtAsc(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_TEAM_NOT_FOUND));

                sos.setTeam(team);

                // =====================
                // Snapshot
                // =====================

                AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                                .findLatestSnapshotByAreaId(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_RISK_NOT_FOUND));

                EnvironmentRisk environmentRisk = environmentRiskEvaluator.evaluate(snapshot);

                Priority priority = sosPriorityCalculator.calculate(
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);

                String priorityReason = priorityReasonGenerator.generate(
                                priority,
                                request.getVictimCount(),
                                request.getInjured(),
                                request.getTrapped(),
                                request.getVulnerable(),
                                environmentRisk);

                sos.setEnvironmentRisk(environmentRisk);

                sos.setPriority(priority);

                sos.setPriorityReason(priorityReason);

                sos.setSnapshotWaterRise(
                                snapshot.getWaterRiseRatePerMinute());

                sos.setSnapshotDangerRatio(
                                snapshot.getDangerRatio());

                sos.setSnapshotPredictionProbability(
                                snapshot.getPredictionProbability());
        }

        private void updateAssignedOrProcessingSos(
                        SosRequest sos,
                        UpdateSosRequest request) {
                sos.setLat(request.getLat());
                sos.setLon(request.getLon());
                sos.setDiachi(request.getDiachi());

                sos.setMota(request.getMota());

                sos.setLastLocationUpdate(
                                LocalDateTime.now());
        }

        private void updateAssignedOrProcessingAnonymousSos(
                        SosRequest sos,
                        UpdateAnonymousSosRequest request) {

                sos.setLat(request.getLat());
                sos.setLon(request.getLon());
                sos.setDiachi(request.getDiachi());

                sos.setMota(request.getMota());

                sos.setLastLocationUpdate(
                                LocalDateTime.now());
        }

        // Get detail sos for người có tài khoản
        @Transactional(readOnly = true)
        public CitizenSosDetailResponse getMySosDetail(UUID sosId) {

                User currentUser = authenticationService.getCurrentUser();

                SosRequest sos = sosRequestRepository
                                .findByIdAndUserId(sosId, currentUser.getId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SOS_NOT_FOUND));

                List<CitizenAssignmentResponse> assignments = sosAssignmentRepository
                                .findBySosId(sosId)
                                .stream()
                                .map(assignment -> CitizenAssignmentResponse.builder()
                                                .groupName(assignment.getGroup().getName())
                                                .groupLeaderName(
                                                                assignment.getGroup().getLeader() != null
                                                                                ? assignment.getGroup().getLeader()
                                                                                                .getHoten()
                                                                                : null)
                                                .groupLeaderPhone(
                                                                assignment.getGroup().getLeader() != null
                                                                                ? assignment.getGroup().getLeader()
                                                                                                .getSodt()
                                                                                : null)
                                                .status(assignment.getStatus())
                                                .role(assignment.getRole())
                                                .build())
                                .toList();

                return CitizenSosDetailResponse.builder()
                                .id(sos.getId())
                                .trackingCode(sos.getTrackingCode())
                                .phoneNumber(sos.getSodt())
                                .victimCount(sos.getVictimCount())
                                .injured(sos.getInjured())
                                .trapped(sos.getTrapped())
                                .vulnerable(sos.getVulnerable())
                                .description(sos.getMota())
                                .lat(sos.getLat())
                                .lon(sos.getLon())
                                .address(sos.getDiachi())
                                .status(sos.getStatus())
                                .createdAt(sos.getCreatedAt())
                                .assignments(assignments)
                                .build();
        }

        //Get detail sos for người không có tài khoản
        @Transactional(readOnly = true)
        public CitizenSosDetailResponse getAnonymousSosDetail(
                        UUID sosId,
                        String sodt,
                        String clientDeviceId) {

                SosRequest sos = sosRequestRepository
                                .findByIdAndSodtAndClientDeviceId(
                                                sosId,
                                                sodt,
                                                clientDeviceId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SOS_NOT_FOUND));

                List<CitizenAssignmentResponse> assignments = sosAssignmentRepository
                                .findBySosId(sosId)
                                .stream()
                                .map(assignment -> CitizenAssignmentResponse.builder()
                                                .groupName(assignment.getGroup().getName())
                                                .groupLeaderName(
                                                                assignment.getGroup().getLeader() != null
                                                                                ? assignment.getGroup().getLeader()
                                                                                                .getHoten()
                                                                                : null)
                                                .groupLeaderPhone(
                                                                assignment.getGroup().getLeader() != null
                                                                                ? assignment.getGroup().getLeader()
                                                                                                .getSodt()
                                                                                : null)
                                                .status(assignment.getStatus())
                                                .role(assignment.getRole())
                                                .build())
                                .toList();

                return CitizenSosDetailResponse.builder()
                                .id(sos.getId())
                                .trackingCode(sos.getTrackingCode())
                                .phoneNumber(sos.getSodt())
                                .victimCount(sos.getVictimCount())
                                .injured(sos.getInjured())
                                .trapped(sos.getTrapped())
                                .vulnerable(sos.getVulnerable())
                                .description(sos.getMota())
                                .lat(sos.getLat())
                                .lon(sos.getLon())
                                .address(sos.getDiachi())
                                .status(sos.getStatus())
                                .createdAt(sos.getCreatedAt())
                                .assignments(assignments)
                                .build();
        }

        // List sos request xếp theo status
        @Transactional(readOnly = true)
        public Page<SosResponse> getMySos(Pageable pageable) {

                User currentUser = authenticationService.getCurrentUser();

                if (currentUser == null) {
                        throw new AppException(
                                        ErrorCode.UNAUTHENTICATED);
                }

                return sosRequestRepository
                                .findMySos(currentUser.getId(), pageable)
                                .map(sosRequestMapper::toResponse);
        }

        // List yêu cầu cho người lạ
        @Transactional(readOnly = true)
        public Page<SosResponse> getAnonymousActiveSos(
                        AnonymousSosListRequest request,
                        Pageable pageable) {

                Page<SosRequest> page = sosRequestRepository
                                .findAnonymousActiveSos(
                                                request.getSodt(),
                                                pageable);
                if (page.isEmpty()) {
                        throw new AppException(
                                        ErrorCode.ACTIVED_SOS_NOT_FOUND);
                }

                return page.map(sosRequestMapper::toResponse);
        }

        // Dashboard cho team leader
        @Cacheable(value = "team-dashboard", key = "#teamId")
        @Transactional(readOnly = true)
        public TeamDashboardResponse getTeamDashboard(UUID teamId) {
                // Test Redis
                log.info("LOAD DASHBOARD FROM DB");

                User currentUser = authenticationService.getCurrentUser();

                RescueTeam team = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                long pending = sosRequestRepository
                                .countByTeamIdAndStatus(
                                                teamId,
                                                StatusSOS.PENDING);

                long assigned = sosRequestRepository
                                .countByTeamIdAndStatus(
                                                teamId,
                                                StatusSOS.ASSIGNED);

                long processing = sosRequestRepository
                                .countByTeamIdAndStatus(
                                                teamId,
                                                StatusSOS.PROCESSING);

                long done = sosRequestRepository
                                .countByTeamIdAndStatus(
                                                teamId,
                                                StatusSOS.DONE);

                long canceled = sosRequestRepository
                                .countByTeamIdAndStatus(
                                                teamId,
                                                StatusSOS.CANCELED);

                return TeamDashboardResponse.builder()
                                .pendingStatus(StatusSOS.PENDING)
                                .pendingCount(pending)
                                .assignedStatus(StatusSOS.ASSIGNED)
                                .assignedCount(assigned)
                                .processingStatus(StatusSOS.PROCESSING)
                                .processingCount(processing)
                                .doneStatus(StatusSOS.DONE)
                                .doneCount(done)
                                .canceledStatus(StatusSOS.CANCELED)
                                .canceledCount(canceled)
                                .totalCount(
                                                pending
                                                                + assigned
                                                                + processing
                                                                + done
                                                                + canceled)
                                .build();
        }

        // Lấy danh sách SOS của team mình
        @Transactional(readOnly = true)
        public Page<SosResponse> getMyTeamSos(Pageable pageable) {

                User currentUser = authenticationService.getCurrentUser();

                RescueTeam team = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                return sosRequestRepository.findActiveByTeamId(team.getId(), pageable)
                                .map(sosRequestMapper::toResponse);
        }

        // Xem chi tiết sos
        @Transactional(readOnly = true)
        public SosDetailResponse getDetail(UUID sosId) {

                SosRequest sos = sosRequestRepository.findById(sosId)
                                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

                List<SosAssignmentResponse> assignments = sosAssignmentRepository.findBySosId(sosId)
                                .stream()
                                .map(sosAssignmentMapper::toResponse)
                                .toList();

                List<SupportRequestResponse> supportRequests = supportRequestRepository.findBySosId(sosId)
                                .stream()
                                .map(supportRequestMapper::toResponse)
                                .toList();

                return SosDetailResponse.builder()
                                .id(sos.getId())

                                .teamId(sos.getTeam().getId())
                                .teamName(sos.getTeam().getName())

                                .phoneNumber(sos.getSodt())

                                .victimCount(sos.getVictimCount())

                                .injured(sos.getInjured())
                                .trapped(sos.getTrapped())
                                .vulnerable(sos.getVulnerable())

                                .description(sos.getMota())

                                .priority(sos.getPriority())
                                .priorityReason(sos.getPriorityReason())

                                .environmentRisk(sos.getEnvironmentRisk())

                                .lat(sos.getLat())
                                .lon(sos.getLon())

                                .address(sos.getDiachi())

                                .status(sos.getStatus())

                                .trackingCode(sos.getTrackingCode())

                                .createdAt(sos.getCreatedAt())

                                .assignments(assignments)

                                .supportRequests(supportRequests)

                                .build();
        }

        public Page<SosResponse> getMyTeamSosByStatus(StatusSOS status, Pageable pageable) {

                User currentUser = authenticationService.getCurrentUser();

                RescueTeam team = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                return sosRequestRepository.findByTeamIdAndStatus(team.getId(), status, pageable)
                                .map(sosRequestMapper::toResponse);

        }

        // Cancel sos người ẩn danh
        @Transactional
        @CacheEvict(value = "team-dashboard", allEntries = true)
        public void cancelAnonymous(
                        UUID sosId,
                        String sodt,
                        String clientDeviceId) {

                SosRequest sos = sosRequestRepository
                                .findByIdAndSodtAndClientDeviceId(
                                                sosId,
                                                sodt,
                                                clientDeviceId)
                                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

                if (sos.getStatus() != StatusSOS.PENDING) {
                        throw new AppException(ErrorCode.SOS_CANNOT_CANCEL);
                }

                sos.setStatus(StatusSOS.CANCELED);

                sosRequestRepository.save(sos);
        }

        // Cancel người có tài khoản
        @Transactional
        @CacheEvict(value = "team-dashboard", allEntries = true)
        public void cancel(UUID sosId) {

                User currentUser = authenticationService.getCurrentUser();

                if (currentUser == null) {
                        throw new AppException(ErrorCode.UNAUTHENTICATED);
                }

                SosRequest sos = sosRequestRepository
                                .findByIdAndUserId(sosId, currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

                if (sos.getStatus() != StatusSOS.PENDING) {
                        throw new AppException(ErrorCode.SOS_CANNOT_CANCEL);
                }

                sos.setStatus(StatusSOS.CANCELED);

                sosRequestRepository.save(sos);
        }

        // Tra cứu sos theo tracking code cho người dân
        @Transactional(readOnly = true)
        public SosResponse getByTrackingCode(
                        String trackingCode) {
                if (trackingCode == null || trackingCode.isBlank()) {
                        throw new AppException(ErrorCode.TRACKING_CODE_REQUIRED);
                }

                SosRequest sos = sosRequestRepository
                                .findByTrackingCode(trackingCode)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SOS_NOT_FOUND));

                SosResponse response = sosRequestMapper.toResponse(sos);

                response.setAlreadyExists(false);

                return response;
        }
}
