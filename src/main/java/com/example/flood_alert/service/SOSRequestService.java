package com.example.flood_alert.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.dbo.request.UpdateAnonymousSosRequest;
import com.example.flood_alert.dbo.request.UpdateSosRequest;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.AreaRiskSnapshot;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.EnvironmentRisk;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.SosRequestMapper;
import com.example.flood_alert.repository.AreaRepository;
import com.example.flood_alert.repository.AreaRiskSnapshotRepository;
import com.example.flood_alert.repository.SosRequestRepository;
import com.example.flood_alert.repository.UserRepository;

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

        EnvironmentRiskEvaluator environmentRiskEvaluator;

        SosPriorityCalculator sosPriorityCalculator;

        SeverityScoreCalculator severityScoreCalculator;

        PriorityReasonGenerator priorityReasonGenerator;

        UserRepository userRepository;

        SosRequestMapper sosRequestMapper;

        List<StatusSOS> ACTIVE_STATUSES = List.of(
                        StatusSOS.PENDING,
                        StatusSOS.PROCESSING);

        @Transactional
        public SosResponse create(CreateSosRequest request, HttpServletRequest httpRequest) {

                User currentUser = getCurrentUser();

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

                // 2. Lấy snapshot mới nhất của khu vực
                AreaRiskSnapshot snapshot = areaRiskSnapshotRepository
                                .findLatestSnapshotByAreaId(areaId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.AREA_RISK_NOT_FOUND));

                // 3. Tính EnvironmentRisk
                EnvironmentRisk environmentRisk = environmentRiskEvaluator.evaluate(snapshot);

                // 4. Tính Priority
                Priority priority = sosPriorityCalculator.calculate(request, environmentRisk);

                // 5. Tính BaseSeverityScore
                Integer baseSeverityScore = severityScoreCalculator.calculate(request, environmentRisk);

                // 6. Sinh PriorityReason
                String priorityReason = priorityReasonGenerator.generate(
                                priority,
                                request,
                                environmentRisk);

                // 7. Tạo SOS
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

                                .baseSeverityScore(
                                                baseSeverityScore)

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

                                .build();

                // Hibernate flush xuống DB ngay lập tức và đồng bộ lại entity
                sos = sosRequestRepository.saveAndFlush(sos);

                // 8. Response
                SosResponse response = sosRequestMapper.toResponse(sos);

                response.setAlreadyExists(false);

                return response;
        }

        private User getCurrentUser() {

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                // log.info("Authentication={}", authentication);

                // if (authentication != null) {
                // log.info("Principal={}", authentication.getPrincipal());
                // log.info("Name={}", authentication.getName());
                // log.info("Authenticated={}", authentication.isAuthenticated());
                // }

                if (authentication == null
                                || !authentication.isAuthenticated()
                                || "anonymousUser".equals(
                                                authentication.getPrincipal())) {

                        return null;
                }

                UUID userId = UUID.fromString(
                                authentication.getName());

                return userRepository.findById(userId)
                                .orElse(null);
        }

        @Transactional
        public SosResponse update(
                        UUID sosId,
                        UpdateSosRequest request,
                        HttpServletRequest httpRequest) {

                User currentUser = getCurrentUser();

                boolean anonymous = currentUser == null;

                SosRequest sos;

                if (anonymous) {

                        throw new AppException(ErrorCode.UNAUTHORIZED_UPDATE_SOS);

                } else {

                        sos = sosRequestRepository
                                        .findByIdAndUserId(sosId, currentUser.getId())
                                        .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));
                }

                if (sos.getStatus() != StatusSOS.PENDING && sos.getStatus() != StatusSOS.PROCESSING) {

                        throw new AppException(ErrorCode.SOS_CANNOT_UPDATE);
                }

                sos.setVictimCount(request.getVictimCount());

                sos.setLat(request.getLat());

                sos.setLon(request.getLon());

                sos.setDiachi(request.getDiachi());

                sos.setAccuracy(request.getAccuracy());

                sos.setInjured(request.getInjured());

                sos.setTrapped(request.getTrapped());

                sos.setVulnerable(request.getVulnerable());

                sos.setMota(request.getMota());

                sos.setLastLocationUpdate(
                                java.time.LocalDateTime.now());

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
                sos.setVictimCount(request.getVictimCount());

                sos.setLat(request.getLat());

                sos.setLon(request.getLon());

                sos.setDiachi(request.getDiachi());

                sos.setAccuracy(request.getAccuracy());

                sos.setInjured(request.getInjured());

                sos.setTrapped(request.getTrapped());

                sos.setVulnerable(request.getVulnerable());

                sos.setMota(request.getMota());

                sos.setLastLocationUpdate(
                                java.time.LocalDateTime.now());

                sos = sosRequestRepository.save(sos);

                SosResponse response = sosRequestMapper.toResponse(sos);

                response.setAlreadyExists(false);

                return response;
        }
}
