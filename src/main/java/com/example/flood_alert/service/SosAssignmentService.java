package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.AssignGroupRequest;
import com.example.flood_alert.dbo.request.AssignSupportGroupRequest;
import com.example.flood_alert.dbo.request.FailAssignmentRequest;
import com.example.flood_alert.dbo.request.UpdateAssignmentStatusRequest;
import com.example.flood_alert.dbo.response.AssignCandidateGroupResponse;
import com.example.flood_alert.dbo.response.AssignmentStatusOptionResponse;
import com.example.flood_alert.dbo.response.GroupAssignmentResponse;
import com.example.flood_alert.dbo.response.SupportGroupResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.SupportRequestItem;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.enums.SupportRequestItemStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.AssignCandidateGroupMapper;
import com.example.flood_alert.repository.RescueGroupMemberRepository;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SosAssignmentRepository;
import com.example.flood_alert.repository.SosRequestRepository;
import com.example.flood_alert.repository.SupportRequestItemRepository;
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
        SosAssignmentRepository sosAssignmentRepository;
        SosRequestRepository sosRequestRepository;
        RescueTeamRepository rescueTeamRepository;
        RescueGroupRepository rescueGroupRepository;
        UserRepository userRepository;
        AuthenticationService authenticationService;
        SupportRequestItemRepository supportRequestItemRepository;
        NotificationManagerService notificationManagerService;
        AssignCandidateGroupMapper assignCandidateGroupMapper;
        RescueGroupMemberRepository rescueGroupMemberRepository;

        // Dispatcher giao nhiệm vụ cho Rescue Group
        @CacheEvict(value = "team-dashboard", allEntries = true)
        @Transactional
        public UUID assignGroup(AssignGroupRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                SosRequest sos = sosRequestRepository
                                .findById(request.getSosId())
                                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));
                validateDispatcherPermission(sos, currentUser);
                RescueTeam team = sos.getTeam();
                RescueGroup group = rescueGroupRepository
                                .findById(request.getGroupId())
                                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

                // Chỉ được giao Group thuộc Team phụ trách SOS
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

        @Transactional(readOnly = true)
        public List<AssignCandidateGroupResponse> getAssignCandidateGroups(UUID sosId) {

                User currentUser = authenticationService.getCurrentUser();

                SosRequest sos = sosRequestRepository.findById(sosId)
                                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

                // Kiểm tra quyền điều phối
                validateDispatcherPermission(sos, currentUser);

                List<RescueGroup> groups = rescueGroupRepository.findAvailableByTeamId(
                                sos.getTeam().getId());

                return groups.stream()
                                .map(group -> {

                                        AssignCandidateGroupResponse response = assignCandidateGroupMapper
                                                        .toResponse(group);

                                        response.setMemberCount(
                                                        rescueGroupMemberRepository.countByGroup_Id(group.getId()));

                                        return response;
                                })
                                .toList();
        }

        /**
         * Kiểm tra quyền điều phối SOS.
         *
         * Quy tắc:
         * - Nếu chưa có Dispatcher => Team Leader được điều phối.
         * - Nếu đã có Dispatcher => chỉ Dispatcher được điều phối.
         */
        private void validateDispatcherPermission(
                        SosRequest sos,
                        User currentUser) {

                // Chưa có người nhận điều phối
                if (sos.getDispatcherUser() == null) {

                        RescueTeam team = sos.getTeam();

                        if (team.getLeader() == null
                                        || !team.getLeader().getId().equals(currentUser.getId())) {

                                throw new AppException(ErrorCode.NO_PERMISSION);
                        }

                        return;
                }

                // Đã có Dispatcher
                if (!sos.getDispatcherUser().getId().equals(currentUser.getId())) {
                        throw new AppException(ErrorCode.NO_PERMISSION);
                }
        }

        // Cập nhật trạng thái SOS theo các Assignment
        private void updateSosStatus(UUID sosId) {

                List<SosAssignment> assignments = sosAssignmentRepository.findBySosId(sosId);

                boolean hasProcessing = assignments.stream()
                                .anyMatch(a -> a.getStatus() == AssignmentStatus.ACKNOWLEDGED
                                                || a.getStatus() == AssignmentStatus.MOVING
                                                || a.getStatus() == AssignmentStatus.ARRIVED
                                                || a.getStatus() == AssignmentStatus.RESCUING);

                boolean allCompleted = !assignments.isEmpty()
                                && assignments.stream()
                                                .allMatch(a -> a.getStatus() == AssignmentStatus.COMPLETED);

                boolean hasFailed = assignments.stream()
                                .anyMatch(a -> a.getStatus() == AssignmentStatus.FAILED);

                SosRequest sos = sosRequestRepository.findById(sosId)
                                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

                if (allCompleted) {

                        sos.setStatus(StatusSOS.DONE);

                } else if (hasProcessing || hasFailed) {

                        sos.setStatus(StatusSOS.PROCESSING);
                }

                sosRequestRepository.save(sos);
        }

        // Trả về danh sách các status assignment
        @Transactional(readOnly = true)
        public List<AssignmentStatusOptionResponse> getAvailableStatuses(UUID assignmentId) {
                User currentUser = authenticationService.getCurrentUser();

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
                                List.of(AssignmentStatus.MOVING);

                        case MOVING ->
                                List.of(AssignmentStatus.ARRIVED);

                        case ARRIVED ->
                                List.of(AssignmentStatus.RESCUING);

                        case RESCUING ->
                                List.of(AssignmentStatus.COMPLETED);

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
                User currentUser = authenticationService.getCurrentUser();

                // Không tìm thấy nhiệm vụ
                SosAssignment assignment = sosAssignmentRepository.findById(assignmentId)
                                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

                // Không phải group leader
                if (!assignment.getGroup().getLeader().getId().equals(currentUser.getId())) {
                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                AssignmentStatus newStatus = request.getStatus();

                // Fail hiện tách thành 1 api riêng
                if (newStatus == AssignmentStatus.FAILED) {
                        throw new AppException(ErrorCode.INVALID_ASSIGNMENT_STATUS);
                }

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

                                // Nếu đây là assignment hỗ trợ
                                if (assignment.getSupportRequestItem() != null) {

                                        SupportRequestItem item = assignment.getSupportRequestItem();

                                        item.setCompletedGroupCount(item.getCompletedGroupCount() + 1);

                                        // Khi tất cả các group đã hoàn thành
                                        if (item.getCompletedGroupCount() >= item.getRequiredGroupCount()) {

                                                item.setStatus(SupportRequestItemStatus.COMPLETED);
                                        }

                                        supportRequestItemRepository.save(item);
                                }
                        }
                        default -> {
                        }
                }
                sosAssignmentRepository.save(assignment);
                updateSosStatus(assignment.getSos().getId());
        }

        // Danh sách nhiệm vụ của group
        @Transactional(readOnly = true)
        public List<GroupAssignmentResponse> getMyAssignments() {

                User currentUser = authenticationService.getCurrentUser();

                return sosAssignmentRepository
                                .findMyAssignments(
                                                currentUser.getId())
                                .stream()
                                .map(this::toGroupResponse)
                                .toList();
        }

        private GroupAssignmentResponse toGroupResponse(
                        SosAssignment assignment) {

                GroupAssignmentResponse.GroupAssignmentResponseBuilder builder = GroupAssignmentResponse.builder()
                                .assignmentId(assignment.getId())
                                .sosId(assignment.getSos().getId())
                                .role(assignment.getRole())
                                .status(assignment.getStatus())
                                .priority(assignment.getSos().getPriority())
                                .lat(assignment.getSos().getLat())
                                .lon(assignment.getSos().getLon());

                // Nếu group đang xem danh sách là group hỗ trợ
                if (assignment.getRole() == AssignmentRole.SUPPORT) {

                        sosAssignmentRepository
                                        .findPrimaryAssignment(
                                                        assignment.getSos().getId())
                                        .ifPresent(primary -> {

                                                builder.primaryGroupId(
                                                                primary.getGroup().getId());

                                                builder.primaryGroupName(
                                                                primary.getGroup().getName());
                                        });
                }

                // Nếu group đang xem danh sách là group chính
                if (assignment.getRole() == AssignmentRole.PRIMARY) {

                        List<SupportGroupResponse> supportGroups = sosAssignmentRepository
                                        .findSupportAssignments(
                                                        assignment.getSos().getId())
                                        .stream()
                                        .map(sa -> SupportGroupResponse
                                                        .builder()
                                                        .groupId(
                                                                        sa.getGroup().getId())
                                                        .groupName(
                                                                        sa.getGroup().getName())
                                                        .status(
                                                                        sa.getStatus())
                                                        .build())
                                        .toList();

                        builder.supportGroups(supportGroups);
                }

                return builder.build();
        }

        // Group leader báo FAILED
        @Transactional
        public void failed(UUID assignmentId, FailAssignmentRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                // Tìm assignment
                SosAssignment assignment = sosAssignmentRepository.findById(assignmentId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.ASSIGNMENT_NOT_FOUND));

                RescueGroup group = assignment.getGroup();

                // Chỉ Group Leader mới được báo FAILED
                if (group.getLeader() == null
                                || !group.getLeader().getId().equals(currentUser.getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Chỉ cho FAILED khi đang thực hiện nhiệm vụ
                if (assignment.getStatus() != AssignmentStatus.ACKNOWLEDGED
                                && assignment.getStatus() != AssignmentStatus.MOVING
                                && assignment.getStatus() != AssignmentStatus.ARRIVED
                                && assignment.getStatus() != AssignmentStatus.RESCUING) {

                        throw new AppException(ErrorCode.INVALID_ASSIGNMENT_STATUS);
                }

                // Cập nhật nhiệm vụ
                assignment.setStatus(AssignmentStatus.FAILED);
                assignment.setFailedReason(request.getFailedReason());
                assignment.setFailedNote(request.getFailedNote());
                assignment.setFailedAt(LocalDateTime.now());

                // Cập nhật trạng thái group
                switch (request.getFailedReason()) {

                        case BOAT_BROKEN,
                                        VEHICLE_BROKEN,
                                        LOST_CONTACT ->
                                group.setStatus(RescueGroupStatus.OFFLINE);

                        case CANNOT_ACCESS,
                                        OTHER ->
                                group.setStatus(RescueGroupStatus.AVAILABLE);
                }

                // Lưu dữ liệu
                sosAssignmentRepository.save(assignment);
                rescueGroupRepository.save(group);

                // Gửi thông báo cho Team Leader
                notificationManagerService.notifyAssignmentFailed(
                                group.getTeam().getLeader(),
                                assignment);

                // Cập nhật trạng thái SOS
                updateSosStatus(assignment.getSos().getId());
        }

}
