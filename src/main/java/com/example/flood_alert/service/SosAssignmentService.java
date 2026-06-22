package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.AssignGroupRequest;
import com.example.flood_alert.dbo.request.UpdateAssignmentStatusRequest;
import com.example.flood_alert.dbo.response.AssignmentStatusOptionResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SosAssignmentRepository;
import com.example.flood_alert.repository.SosRequestRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SosAssignmentService {
    private final AlertService alertService;
    SosAssignmentRepository sosAssignmentRepository;
    SosRequestRepository sosRequestRepository;
    RescueTeamRepository rescueTeamRepository;
    RescueGroupRepository rescueGroupRepository;
    UserRepository userRepository;

    // Giao nhiệm vụ cho group từ team leader
    @CacheEvict(value = "team-dashboard", allEntries = true)
    @Transactional
    public UUID assignGroup(UUID sosId, UUID groupId, AssignmentRole role, String note, UUID leaderId) {
        // Tìm yêu cầu sos theo id
        SosRequest sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

        // Tìm group theo id
        RescueGroup group = rescueGroupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_GROUP_NOT_FOUND));

        // Tìm team leader theo id
        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra xem người đó có phải leader của team ko
        if (!group.getTeam().getId().equals(leader.getTeam().getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        SosAssignment assignment = SosAssignment.builder()
                .sos(sos)
                .group(group)
                .assignedBy(leader)
                .role(role)
                .status(AssignmentStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .note(note)
                .build();
        sosAssignmentRepository.save(assignment).getId();
        // Cập nhật trạng thái cho yêu cầu sos
        sos.setStatus(StatusSOS.ASSIGNED);
        sosRequestRepository.save(sos);
        return assignment.getId();
    }

    // Hàm cập nhật trạng thái cho nhiệm vụ do group leader làm
    public void updateStatus(UUID assignmentId, AssignmentStatus newStatus, String note, UUID currentUserId) {
        // Tìm kiếm nhiệm vụ theo Id
        SosAssignment assignment = sosAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // Kiem tra nguoi dung hien tai co phai la leader cua group khong
        if (!assignment.getGroup().getLeader().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        assignment.setStatus(newStatus);
        switch (newStatus) {
            case ACKNOWLEDGED ->
                assignment.setAcknowledgedAt(LocalDateTime.now());
            case ARRIVED -> assignment.setArrivedAt(LocalDateTime.now());
            case COMPLETED -> assignment.setCompletedAt(LocalDateTime.now());
            default ->
                {

                }
        }
        assignment.setNote(note);
        sosAssignmentRepository.save(assignment);
        updateSosStatus(assignment.getSos().getId());
    }

    // Cập nhật trạng thái cho yêu cầu sos
    private void updateSosStatus(UUID sosId) {
        List<SosAssignment> assignments = sosAssignmentRepository.findBySosId(sosId);

        boolean hasProcessing = assignments.stream()
                .anyMatch(a -> a.getStatus() == AssignmentStatus.ACKNOWLEDGED
                        || a.getStatus() == AssignmentStatus.MOVING
                        || a.getStatus() == AssignmentStatus.ARRIVED
                        || a.getStatus() == AssignmentStatus.RESCUING);
        boolean allCompleted = !assignments.isEmpty()
                &&
                assignments.stream()
                        .allMatch(a -> a.getStatus() == AssignmentStatus.COMPLETED);

        SosRequest sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

        if (allCompleted) {
            sos.setStatus(StatusSOS.DONE);
        } else if (hasProcessing) {
            sos.setStatus(StatusSOS.PROCESSING);
        }

        sosRequestRepository.save(sos);
    }

    // Leader giao nhiệm vụ cho group
    @Transactional
    public UUID assignGroup(AssignGroupRequest request) {

        User currentUser = getCurrentUser();

        RescueTeam team = rescueTeamRepository.findByLeaderId(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

        SosRequest sos = sosRequestRepository
                .findById(request.getSosId())
                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

        // SOS phải thuộc Team của Leader
        if (!sos.getTeam().getId().equals(team.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        RescueGroup group = rescueGroupRepository
                .findById(request.getGroupId())
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        // Chỉ được assign Group trong Team mình
        if (!group.getTeam().getId().equals(team.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        // Group phải đang AVAILABLE
        if (group.getStatus() != RescueGroupStatus.AVAILABLE) {
            throw new AppException(ErrorCode.GROUP_NOT_AVAILABLE);
        }

        SosAssignment assignment = SosAssignment.builder()
                .sos(sos)
                .group(group)
                .assignedBy(currentUser)
                .role(request.getRole())
                .status(AssignmentStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .note(request.getNote())
                .build();

        sosAssignmentRepository.save(assignment);

        // SOS chuyển sang ASSIGNED
        if (sos.getStatus() == StatusSOS.PENDING) {
            sos.setStatus(StatusSOS.ASSIGNED);
            sosRequestRepository.save(sos);
        }

        // Group chuyển BUSY
        group.setStatus(RescueGroupStatus.BUSY);
        rescueGroupRepository.save(group);

        return assignment.getId();
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

    // Trả về danh sách các status assignment
    @Transactional(readOnly = true)
    public List<AssignmentStatusOptionResponse> getAvailableStatuses(UUID assignmentId) {
        User currentUser = getCurrentUser();

        SosAssignment assignment = sosAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // Không phải group leader
        if (!assignment.getGroup().getLeader().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        List<AssignmentStatus> nextStatus = getNextStatuses(assignment.getStatus());

        return nextStatus.stream()
                .map(status -> AssignmentStatusOptionResponse.builder()
                        .code(status.name())
                        .name(getDisplayName(status))
                        .build())
                .toList();
    }

    // Rule chuyển trạng thái
    private List<AssignmentStatus> getNextStatuses(
            AssignmentStatus currentStatus) {

        return switch (currentStatus) {

            case ASSIGNED ->
                List.of(AssignmentStatus.ACKNOWLEDGED);

            case ACKNOWLEDGED ->
                List.of(
                        AssignmentStatus.MOVING,
                        AssignmentStatus.FAILED);

            case MOVING ->
                List.of(
                        AssignmentStatus.ARRIVED,
                        AssignmentStatus.FAILED);

            case ARRIVED ->
                List.of(
                        AssignmentStatus.RESCUING,
                        AssignmentStatus.FAILED);

            case RESCUING ->
                List.of(
                        AssignmentStatus.COMPLETED,
                        AssignmentStatus.FAILED);

            case COMPLETED, FAILED ->
                Collections.emptyList();
        };
    }

    // Tên hiển thị trạng thái
    private String getDisplayName(
            AssignmentStatus status) {

        return switch (status) {

            case ASSIGNED -> "Đã giao nhiệm vụ";

            case ACKNOWLEDGED -> "Đã xác nhận";

            case MOVING -> "Đang di chuyển";

            case ARRIVED -> "Đã đến hiện trường";

            case RESCUING -> "Đang cứu hộ";

            case COMPLETED -> "Hoàn thành";

            case FAILED -> "Thất bại";
        };
    }

    // Group leader nhận nhiệm vụ và cập nhật status
    @Transactional
    public void updateStatus(UUID assignmentId, UpdateAssignmentStatusRequest request) {
        User currentUser = getCurrentUser();

        // Không tìm thấy nhiệm vụ
        SosAssignment assignment = sosAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        // Không phải group leader
        if (!assignment.getGroup().getLeader().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        AssignmentStatus newStatus = request.getStatus();

        assignment.setStatus(newStatus);

        assignment.setNote(request.getNote());

        switch (newStatus) {
            case ACKNOWLEDGED -> assignment.setAcknowledgedAt(LocalDateTime.now());
            case ARRIVED -> assignment.setArrivedAt(LocalDateTime.now());
            case COMPLETED -> {
                assignment.setCompletedAt(LocalDateTime.now());

                RescueGroup group = assignment.getGroup();
                group.setStatus(RescueGroupStatus.AVAILABLE);

                rescueGroupRepository.save(group);
            }
            default -> {
            }
        }
        sosAssignmentRepository.save(assignment);
        updateSosStatus(assignment.getSos().getId());
    }
}
