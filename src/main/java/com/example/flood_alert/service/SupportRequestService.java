package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.ApproveSupportRequest;
import com.example.flood_alert.dbo.request.AssignSupportGroupRequest;
import com.example.flood_alert.dbo.request.CreateSupportRequest;
import com.example.flood_alert.dbo.request.RejectAssignedSupportRequest;
import com.example.flood_alert.dbo.request.RejectSupportRequest;
import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.SupportRequestStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SosAssignmentRepository;
import com.example.flood_alert.repository.SosRequestRepository;
import com.example.flood_alert.repository.SupportRequestRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SupportRequestService {
        SupportRequestRepository supportRequestRepository;
        SosRequestRepository sosRequestRepository;
        SosAssignmentRepository sosAssignmentRepository;
        RescueTeamRepository rescueTeamRepository;
        RescueGroupRepository groupRepository;
        UserRepository userRepository;
        AuthenticationService authenticationService;

        // Tạo yêu cầu hỗ trợ cứu hộ, teamleader tạo
        public UUID create(CreateSupportRequest request) {
                // Tìm thông tin người gửi yêu cầu
                User currentUser = authenticationService.getCurrentUser();

                // Tìm sos theo id
                SosRequest sos = sosRequestRepository.findById(request.getSosId())
                                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

                // Nếu người dùng ko có team
                if (currentUser.getTeam() == null) {
                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Kiểm tra người dùng hiện tại có phải leader của team đó ko
                RescueTeam team = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                // Kiểm tra sos có thuộc team của leader đó ko
                if (!sos.getTeam().getId().equals(team.getId())) {
                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Kiểm tra xem leader đã tạo hỗ trợ đó chưa
                if (supportRequestRepository.existsBySosIdAndStatus(request.getSosId(), SupportRequestStatus.PENDING)) {

                        throw new AppException(ErrorCode.SUPPORT_REQUEST_ALREADY_EXISTS);
                }

                // Tạo đối tượng group
                RescueGroup suggestedGroup = null;

                // Nếu không tìm thấy group theo id
                if (request.getSuggestedGroupId() != null) {
                        suggestedGroup = groupRepository.findById(request.getSuggestedGroupId())
                                        .orElseThrow(() -> new AppException(ErrorCode.RESCUE_GROUP_NOT_FOUND));
                }

                // Tạo yêu cầu hỗ trợ
                SupportRequest supportRequest = SupportRequest.builder()
                                .sos(sos)
                                .requestedBy(currentUser)
                                .status(SupportRequestStatus.PENDING)
                                .supportType(request.getSupportType())
                                .suggestedGroup(suggestedGroup)
                                .reason(request.getReason())
                                .build();

                supportRequestRepository.save(supportRequest);
                return supportRequest.getId();
        }

        // Hiển thị danh sách các yêu cầu chi viện lên dashboard cho province leader
        @Transactional(readOnly = true)
        public Page<SupportRequestResponse> getByStatus(
                        SupportRequestStatus status,
                        Pageable pageable) {

                User currentUser = authenticationService.getCurrentUser();

                UUID provinceId = currentUser
                                .getArea()
                                .getId();

                return supportRequestRepository
                                .findByProvinceAndStatus(
                                                provinceId,
                                                status,
                                                pageable)
                                .map(this::toResponse);
        }

        // Xem chi tiết yêu cầu
        @Transactional(readOnly = true)
        public SupportRequestResponse getDetail(
                        UUID supportRequestId) {

                SupportRequest request = supportRequestRepository
                                .findById(supportRequestId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

                return toResponse(request);
        }

        // Danh sách yêu cầu theo mã yêu cầu
        @Transactional(readOnly = true)
        public List<SupportRequestResponse> findBySos(UUID sosId) {
                return supportRequestRepository.findBySosId(sosId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        private SupportRequestResponse toResponse(SupportRequest request) {

                return SupportRequestResponse.builder()
                                .id(request.getId())
                                .sosId(request.getSos().getId())
                                .status(request.getStatus())
                                .supportType(request.getSupportType())
                                .reason(request.getReason())
                                .provinceResponse(request.getProvinceResponse())
                                .teamResponse(request.getTeamResponse())
                                .requestedById(request.getRequestedBy().getId())
                                .requestedByName(request.getRequestedBy().getHoten())
                                .approvedById(
                                                request.getApprovedBy() != null
                                                                ? request.getApprovedBy().getId()
                                                                : null)

                                .approvedByName(
                                                request.getApprovedBy() != null
                                                                ? request.getApprovedBy().getHoten()
                                                                : null)
                                .assignedTeamId(
                                                request.getAssignedTeam() != null
                                                                ? request.getAssignedTeam().getId()
                                                                : null)
                                .assignedTeamName(
                                                request.getAssignedTeam() != null
                                                                ? request.getAssignedTeam().getName()
                                                                : null)
                                .createdAt(request.getCreatedAt())
                                .reviewedAt(request.getReviewedAt())
                                .build();
        }

        // Chấp nhận yêu cầu hỗ trợ
        @Transactional
        public void approve(
                        UUID supportRequestId,
                        ApproveSupportRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                SupportRequest supportRequest = supportRequestRepository
                                .findById(supportRequestId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

                // Kiểm tra xem yêu cầu sos có thuộc tỉnh mình quản lý ko
                if (!supportRequest.getSos()
                                .getArea()
                                .getParent()
                                .getId()
                                .equals(currentUser.getArea().getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Kiểm tra xem trạng thái hỗ trợ đã được phê duyệt chưa
                if (supportRequest.getStatus() != SupportRequestStatus.PENDING) {

                        throw new AppException(ErrorCode.SUPPORT_REQUEST_ALREADY_REVIEWED);
                }

                // Kiểm tra team phân bố có tồn tại ko
                RescueTeam team = rescueTeamRepository
                                .findById(request.getAssignedTeamId())
                                .orElseThrow(() -> new AppException(ErrorCode.RESCUE_TEAM_NOT_FOUND));

                supportRequest.setStatus(SupportRequestStatus.COMPLETED);

                supportRequest.setApprovedBy(currentUser);

                supportRequest.setAssignedTeam(team);
                ;

                supportRequest.setProvinceResponse(request.getProvinceResponse());

                supportRequest.setReviewedAt(LocalDateTime.now());

                supportRequestRepository.save(supportRequest);
        }

        // Từ chối hỗ trợ
        @Transactional
        public void reject(
                        UUID supportRequestId,
                        RejectSupportRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                SupportRequest supportRequest = supportRequestRepository
                                .findById(supportRequestId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

                // Kiểm tra xem yêu cầu sos có thuộc tỉnh mình quản lý ko
                if (!supportRequest.getSos()
                                .getArea()
                                .getParent()
                                .getId()
                                .equals(currentUser.getArea().getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Kiểm tra xem trạng thái hỗ trợ đã được phê duyệt chưa
                if (supportRequest.getStatus() != SupportRequestStatus.PENDING) {

                        throw new AppException(ErrorCode.SUPPORT_REQUEST_ALREADY_REVIEWED);
                }

                supportRequest.setStatus(SupportRequestStatus.REJECTED);

                supportRequest.setApprovedBy(currentUser);

                supportRequest.setProvinceResponse(request.getProvinceResponse());

                supportRequest.setReviewedAt(LocalDateTime.now());

                supportRequestRepository.save(supportRequest);
        }

        // Team leader từ chối yêu cầu chi viện
        @Transactional
        public void teamReject(
                        UUID supportRequestId,
                        RejectAssignedSupportRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                // Load đơn hỗ trợ
                SupportRequest supportRequest = supportRequestRepository
                                .findById(supportRequestId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

                // Chỉ cho từ chối khi status đang APPROVED
                if (supportRequest.getStatus() != SupportRequestStatus.APPROVED) {

                        throw new AppException(ErrorCode.INVALID_SUPPORT_REQUEST_STATUS);
                }

                // Kiểm tra team leader
                RescueTeam myTeam = rescueTeamRepository
                                .findByLeaderId(
                                                currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                // Kiểm tra có đúng team đc giao không
                if (!supportRequest
                                .getAssignedTeam()
                                .getId()
                                .equals(myTeam.getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Từ chối chi viện
                supportRequest.setStatus(SupportRequestStatus.TEAM_REJECTED);

                supportRequest.setTeamResponse(request.getReason());

                supportRequest.setReviewedAt(LocalDateTime.now());

                supportRequestRepository.save(supportRequest);
        }

        // Team leader xem danh sách yêu cầu đc tỉnh giao cho
        @Transactional(readOnly = true)
        public Page<SupportRequestResponse> getMyTeamSupportRequests(
                        Pageable pageable) {

                User currentUser = authenticationService.getCurrentUser();

                RescueTeam team = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                return supportRequestRepository
                                .findByAssignedTeamIdAndStatus(
                                                team.getId(),
                                                SupportRequestStatus.APPROVED,
                                                pageable)
                                .map(this::toResponse);
        }

        @Transactional
        public UUID assignGroup(
                        UUID supportRequestId,
                        AssignSupportGroupRequest request) {

                User currentUser = authenticationService.getCurrentUser();

                SupportRequest supportRequest = supportRequestRepository
                                .findById(supportRequestId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_NOT_FOUND));
                // Kiểm tra request support đc duyệt chưa
                if (supportRequest.getStatus() != SupportRequestStatus.APPROVED) {

                        throw new AppException(
                                        ErrorCode.SUPPORT_REQUEST_NOT_APPROVED);
                }
                // Kiểm tra team leader có phải của team đc giao ko
                RescueTeam myTeam = rescueTeamRepository
                                .findByLeaderId(
                                                currentUser.getId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.NO_PERMISSION));

                if (!supportRequest
                                .getAssignedTeam()
                                .getId()
                                .equals(myTeam.getId())) {

                        throw new AppException(
                                        ErrorCode.NO_PERMISSION);
                }

                // Lấy group
                RescueGroup group = groupRepository
                                .findById(request.getGroupId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                if (group.getStatus() != RescueGroupStatus.AVAILABLE) {
                        throw new AppException(ErrorCode.GROUP_NOT_AVAILABLE);
                }

                // Kiểm tra group có thuộc team hỗ trợ
                if (!group.getTeam().getId()
                                .equals(myTeam.getId())) {

                        throw new AppException(
                                        ErrorCode.NO_PERMISSION);
                }

                // Kiểm tra đã giao nhiệm vụ hỗ trợ chưa
                if (sosAssignmentRepository
                                .existsBySosIdAndGroupId(
                                                supportRequest.getSos().getId(),
                                                group.getId())) {

                        throw new AppException(
                                        ErrorCode.SOS_ALREADY_ASSIGNED);
                }

                SosRequest sos = supportRequest.getSos();

                // Tạo assignment
                SosAssignment assignment = SosAssignment.builder()
                                .sos(sos)
                                .group(group)
                                .assignedBy(currentUser)
                                .role(AssignmentRole.SUPPORT)
                                .status(AssignmentStatus.ASSIGNED)
                                .assignedAt(LocalDateTime.now())
                                .note(request.getNote())
                                .build();

                sosAssignmentRepository.save(assignment);

                // Chuyển group sang busy
                group.setStatus(
                                RescueGroupStatus.BUSY);

                groupRepository.save(group);

                // Đánh dấu support request đã được giao cho group
                supportRequest.setStatus(
                                SupportRequestStatus.APPROVED);

                supportRequestRepository.save(
                                supportRequest);

                return assignment.getId();
        }

}
