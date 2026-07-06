package com.example.flood_alert.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.ApproveSupportRequest;
import com.example.flood_alert.dbo.request.ApproveSupportRequestItem;
import com.example.flood_alert.dbo.request.AssignSupportGroupRequest;
import com.example.flood_alert.dbo.request.CreateSupportRequest;
import com.example.flood_alert.dbo.request.CreateSupportRequestItem;
import com.example.flood_alert.dbo.request.RejectAssignedSupportRequest;
import com.example.flood_alert.dbo.response.RescueTeamSupportResponse;
import com.example.flood_alert.dbo.response.SosMarkerResponse;
import com.example.flood_alert.dbo.response.SupportMapResponse;
import com.example.flood_alert.dbo.response.SupportRequestItemResponse;
import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.entity.SupportRequestItem;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;
import com.example.flood_alert.enums.MarkerType;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.SupportRequestItemStatus;
import com.example.flood_alert.enums.SupportRequestStatus;
import com.example.flood_alert.enums.SupportType;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.SosAssignmentRepository;
import com.example.flood_alert.repository.SosRequestRepository;
import com.example.flood_alert.repository.SupportRequestItemRepository;
import com.example.flood_alert.repository.SupportRequestRepository;

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
        AuthenticationService authenticationService;
        SupportRequestItemRepository supportRequestItemRepository;

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
                // kiểm tra sos request có tạo yêu cầu hỗ trợ và đang đợi duyệt ko
                if (supportRequestRepository.existsBySosIdAndStatus(
                                request.getSosId(),
                                SupportRequestStatus.PENDING)) {

                        throw new AppException(
                                        ErrorCode.SUPPORT_REQUEST_ALREADY_EXISTS);
                }

                Set<SupportType> supportTypes = new HashSet<>();

                for (CreateSupportRequestItem item : request.getItems()) {
                        if (!supportTypes.add(item.getSupportType())) {
                                throw new AppException(ErrorCode.DUPLICATE_SUPPORT_TYPE);
                        }
                }

                // Tạo yêu cầu hỗ trợ
                SupportRequest supportRequest = SupportRequest.builder()
                                .sos(sos)
                                .requestedBy(currentUser)
                                .status(SupportRequestStatus.PENDING)
                                .reason(request.getReason())
                                .build();

                // Tạo support request item
                List<SupportRequestItem> items = request.getItems()
                                .stream()
                                .map(item -> SupportRequestItem.builder()
                                                .supportRequest(supportRequest)
                                                .supportType(item.getSupportType())
                                                .requiredGroupCount(item.getRequiredGroupCount())
                                                .assignedGroupCount(0)
                                                .status(SupportRequestItemStatus.PENDING)
                                                .build())
                                .toList();
                supportRequest.setItems(items);
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

                                .items(
                                                request.getItems()
                                                                .stream()
                                                                .map(item -> SupportRequestItemResponse.builder()
                                                                                .id(item.getId())
                                                                                .supportType(item.getSupportType())
                                                                                .requiredGroupCount(item
                                                                                                .getRequiredGroupCount())

                                                                                .status(item.getStatus())

                                                                                .assignedTeamId(
                                                                                                item.getAssignedTeam() != null
                                                                                                                ? item.getAssignedTeam()
                                                                                                                                .getId()
                                                                                                                : null)

                                                                                .assignedTeamName(
                                                                                                item.getAssignedTeam() != null
                                                                                                                ? item.getAssignedTeam()
                                                                                                                                .getName()
                                                                                                                : null)

                                                                                .provinceNote(item.getProvinceNote())

                                                                                .teamResponse(item.getTeamResponse())

                                                                                .build())
                                                                .toList())

                                .reason(request.getReason())

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

                                .createdAt(request.getCreatedAt())

                                .reviewedAt(request.getReviewedAt())

                                .build();
        }

        // Chấp nhận yêu cầu hỗ trợ
        @Transactional
        public void approve(
                        UUID supportRequestId,
                        ApproveSupportRequest request) {

                // Province đang đăng nhập
                User currentUser = authenticationService.getCurrentUser();

                // Tìm yêu cầu hỗ trợ
                SupportRequest supportRequest = supportRequestRepository
                                .findById(supportRequestId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

                // Kiểm tra SOS có thuộc tỉnh mà Province đang quản lý không
                if (!supportRequest.getSos()
                                .getArea()
                                .getParent()
                                .getId()
                                .equals(currentUser.getArea().getId())) {

                        throw new AppException(ErrorCode.NO_PERMISSION);
                }

                // Chỉ được duyệt khi yêu cầu còn ở trạng thái PENDING
                if (supportRequest.getStatus() != SupportRequestStatus.PENDING) {
                        throw new AppException(
                                        ErrorCode.SUPPORT_REQUEST_ALREADY_REVIEWED);
                }

                // Map các item theo id để tìm nhanh
                Map<UUID, SupportRequestItem> itemMap = supportRequest.getItems()
                                .stream()
                                .collect(Collectors.toMap(
                                                SupportRequestItem::getId,
                                                Function.identity()));
                if (request.getItems().size() != supportRequest.getItems().size()) {
                        throw new AppException(
                                        ErrorCode.SUPPORT_REQUEST_NOT_FULLY_REVIEWED);
                }
                // Province xử lý từng hạng mục hỗ trợ
                for (ApproveSupportRequestItem dto : request.getItems()) {

                        // Kiểm tra item có thuộc support request này không
                        SupportRequestItem item = itemMap.get(dto.getSupportRequestItemId());

                        if (item == null) {
                                throw new AppException(
                                                ErrorCode.SUPPORT_REQUEST_ITEM_NOT_FOUND);
                        }

                        switch (dto.getStatus()) {

                                // Province đồng ý điều phối
                                case APPROVED -> {

                                        // Bắt buộc phải chọn Team
                                        if (dto.getAssignedTeamId() == null) {
                                                throw new AppException(
                                                                ErrorCode.ASSIGNED_TEAM_REQUIRED);
                                        }

                                        // Kiểm tra Team tồn tại
                                        RescueTeam team = rescueTeamRepository
                                                        .findById(dto.getAssignedTeamId())
                                                        .orElseThrow(() -> new AppException(
                                                                        ErrorCode.RESCUE_TEAM_NOT_FOUND));

                                        // Kiểm tra Team có thuộc cùng tỉnh với SOS không
                                        if (!team.getArea()
                                                        .getParent()
                                                        .getId()
                                                        .equals(supportRequest.getSos()
                                                                        .getArea()
                                                                        .getParent()
                                                                        .getId())) {

                                                throw new AppException(
                                                                ErrorCode.NO_PERMISSION);
                                        }

                                        // Giao Team cho hạng mục hỗ trợ
                                        item.setAssignedTeam(team);

                                        // Cập nhật trạng thái item
                                        item.setStatus(SupportRequestItemStatus.APPROVED);

                                        // Ghi chú của Province
                                        item.setProvinceNote(dto.getProvinceResponse());
                                }

                                // Province từ chối điều phối hạng mục này
                                case REJECTED -> {

                                        item.setAssignedTeam(null);

                                        item.setStatus(SupportRequestItemStatus.REJECTED);

                                        item.setProvinceNote(dto.getProvinceResponse());
                                }

                                default ->
                                        throw new AppException(
                                                        ErrorCode.INVALID_SUPPORT_ITEM_STATUS);
                        }
                }

                // Đánh dấu Province đã xử lý xong toàn bộ Support Request
                supportRequest.setStatus(
                                SupportRequestStatus.APPROVED);

                supportRequest.setApprovedBy(currentUser);

                supportRequest.setReviewedAt(
                                LocalDateTime.now());

                supportRequestRepository.save(supportRequest);
        }

        // Team leader từ chối hạng mục chi viện
        @Transactional
        public void teamReject(
                        UUID supportRequestItemId,
                        RejectAssignedSupportRequest request) {

                // Team leader đang đăng nhập
                User currentUser = authenticationService.getCurrentUser();

                // Tìm hạng mục hỗ trợ
                SupportRequestItem item = supportRequestItemRepository
                                .findById(supportRequestItemId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_ITEM_NOT_FOUND));

                // Chỉ được từ chối khi Province đã giao Team
                if (item.getStatus() != SupportRequestItemStatus.APPROVED) {
                        throw new AppException(
                                        ErrorCode.INVALID_SUPPORT_REQUEST_STATUS);
                }

                // Kiểm tra người đăng nhập có phải Team Leader không
                RescueTeam myTeam = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.NO_PERMISSION));

                // Kiểm tra hạng mục này có được giao cho Team của mình không
                if (item.getAssignedTeam() == null
                                || !item.getAssignedTeam().getId().equals(myTeam.getId())) {

                        throw new AppException(
                                        ErrorCode.NO_PERMISSION);
                }

                // Cập nhật trạng thái
                item.setStatus(SupportRequestItemStatus.TEAM_REJECTED);

                // Lưu lý do từ chối
                item.setTeamResponse(request.getReason());

                // Bỏ Team hiện tại để Province có thể phân công lại
                item.setAssignedTeam(null);

                supportRequestItemRepository.save(item);
        }

        // Team leader xem danh sách yêu cầu được tỉnh giao cho
        @Transactional(readOnly = true)
        public Page<SupportRequestResponse> getMyTeamSupportRequests(
                        Pageable pageable) {

                User currentUser = authenticationService.getCurrentUser();

                RescueTeam team = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

                return supportRequestRepository
                                .findMySupportRequests(team.getId(), pageable)
                                .map(request -> toResponse(request, team.getId()));
        }

        @Transactional
        public UUID assignGroup(
                        UUID supportRequestItemId,
                        AssignSupportGroupRequest request) {

                // Team leader đang đăng nhập
                User currentUser = authenticationService.getCurrentUser();

                // Tìm hạng mục hỗ trợ
                SupportRequestItem item = supportRequestItemRepository
                                .findById(supportRequestItemId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_ITEM_NOT_FOUND));

                // Chỉ được phân group khi Province đã duyệt
                if (item.getStatus() != SupportRequestItemStatus.APPROVED) {
                        throw new AppException(
                                        ErrorCode.SUPPORT_REQUEST_NOT_APPROVED);
                }

                // Kiểm tra đã phân đủ số group chưa
                if (item.getAssignedGroupCount() >= item.getRequiredGroupCount()) {
                        throw new AppException(
                                        ErrorCode.SUPPORT_GROUP_ALREADY_ASSIGNED_ENOUGH);
                }

                // Team leader của Team được giao
                RescueTeam myTeam = rescueTeamRepository
                                .findByLeaderId(currentUser.getId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.NO_PERMISSION));

                // Kiểm tra item có thuộc Team này không
                if (item.getAssignedTeam() == null
                                || !item.getAssignedTeam().getId().equals(myTeam.getId())) {

                        throw new AppException(
                                        ErrorCode.NO_PERMISSION);
                }

                // Lấy Group
                RescueGroup group = groupRepository
                                .findById(request.getGroupId())
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.RESCUE_GROUP_NOT_FOUND));

                // Group phải thuộc Team
                if (!group.getTeam().getId().equals(myTeam.getId())) {
                        throw new AppException(
                                        ErrorCode.NO_PERMISSION);
                }

                // Group phải AVAILABLE
                if (group.getStatus() != RescueGroupStatus.AVAILABLE) {
                        throw new AppException(
                                        ErrorCode.GROUP_NOT_AVAILABLE);
                }

                // Kiểm tra năng lực Group
                switch (item.getSupportType()) {

                        case BOAT -> {
                                if (!group.isHasBoat()) {
                                        throw new AppException(
                                                        ErrorCode.GROUP_NOT_SUPPORT_TYPE);
                                }
                        }

                        case MEDICAL -> {
                                if (!group.isHasMedical()) {
                                        throw new AppException(
                                                        ErrorCode.GROUP_NOT_SUPPORT_TYPE);
                                }
                        }

                        case SEARCH_RESCUE -> {
                                if (!group.isHasSearchRescue()) {
                                        throw new AppException(
                                                        ErrorCode.GROUP_NOT_SUPPORT_TYPE);
                                }
                        }

                        case LOGISTICS -> {
                                if (!group.isHasLogistics()) {
                                        throw new AppException(
                                                        ErrorCode.GROUP_NOT_SUPPORT_TYPE);
                                }
                        }
                }

                // Không cho phân cùng một Group nhiều lần cho cùng SOS
                if (sosAssignmentRepository.existsBySosIdAndGroupId(
                                item.getSupportRequest().getSos().getId(),
                                group.getId())) {

                        throw new AppException(
                                        ErrorCode.SOS_ALREADY_ASSIGNED);
                }

                // Tạo Assignment
                SosAssignment assignment = SosAssignment.builder()
                                .sos(item.getSupportRequest().getSos())
                                .group(group)
                                .supportRequestItem(item)
                                .assignedBy(currentUser)
                                .role(AssignmentRole.SUPPORT)
                                .status(AssignmentStatus.ASSIGNED)
                                .assignedAt(LocalDateTime.now())
                                .note(request.getNote())
                                .build();

                sosAssignmentRepository.save(assignment);

                // Group chuyển sang BUSY
                group.setStatus(RescueGroupStatus.BUSY);
                groupRepository.save(group);

                // Tăng số group đã phân
                item.setAssignedGroupCount(
                                item.getAssignedGroupCount() + 1);

                supportRequestItemRepository.save(item);

                return assignment.getId();
        }

        // Danh sách các team trong 1 tỉnh: bao gồm vị trí sos, team gửi support và các
        // team lân cận
        @Transactional(readOnly = true)
        public SupportMapResponse getCandidateTeams(
                        UUID supportRequestId) {

                SupportRequest supportRequest = supportRequestRepository
                                .findById(supportRequestId)
                                .orElseThrow(() -> new AppException(
                                                ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

                // Team đang gửi yêu cầu hỗ trợ
                UUID requesterTeamId = supportRequest
                                .getRequestedBy()
                                .getTeam()
                                .getId();

                // Province của SOS
                UUID provinceId = supportRequest
                                .getSos()
                                .getArea()
                                .getParent()
                                .getId();
                SosRequest sos = supportRequest.getSos();
                List<RescueTeamSupportResponse> result = new ArrayList<>();

                // Marker SOS màu đỏ
                SosMarkerResponse sosMarker = SosMarkerResponse.builder()
                                .sosId(sos.getId())
                                .lat(sos.getLat())
                                .lon(sos.getLon())
                                .priority(sos.getPriority())
                                .build();

                List<RescueTeamSupportResponse> teams = rescueTeamRepository
                                .findAllSupportTeams(provinceId)
                                .stream()
                                .map(team -> RescueTeamSupportResponse.builder()

                                                .id(team.getId())
                                                .name(team.getName())

                                                .areaId(team.getArea().getId())
                                                .lat(team.getLat())
                                                .lon(team.getLon())

                                                .leaderName(
                                                                team.getLeader() != null
                                                                                ? team.getLeader().getHoten()
                                                                                : null)

                                                .leaderPhone(
                                                                team.getLeader() != null
                                                                                ? team.getLeader().getSodt()
                                                                                : null)

                                                .emergencyPhone(team.getEmergencyPhone())

                                                .availableBoatGroups(
                                                                groupRepository.countByTeamIdAndHasBoatTrueAndStatus(
                                                                                team.getId(),
                                                                                RescueGroupStatus.AVAILABLE))

                                                .availableMedicalGroups(
                                                                groupRepository.countByTeamIdAndHasMedicalTrueAndStatus(
                                                                                team.getId(),
                                                                                RescueGroupStatus.AVAILABLE))

                                                .availableSearchRescueGroups(
                                                                groupRepository.countByTeamIdAndHasSearchRescueTrueAndStatus(
                                                                                team.getId(),
                                                                                RescueGroupStatus.AVAILABLE))

                                                .availableLogisticsGroups(
                                                                groupRepository.countByTeamIdAndHasLogisticsTrueAndStatus(
                                                                                team.getId(),
                                                                                RescueGroupStatus.AVAILABLE))

                                                .distanceKm(
                                                                calculateDistanceKm(
                                                                                sos.getLat(),
                                                                                sos.getLon(),
                                                                                team.getLat(),
                                                                                team.getLon()))

                                                .markerType(
                                                                team.getId().equals(requesterTeamId)
                                                                                ? MarkerType.REQUESTER_TEAM
                                                                                : MarkerType.CANDIDATE_TEAM)

                                                .build())
                                .toList();

                return SupportMapResponse.builder()
                                .sos(sosMarker)
                                .teams(teams)
                                .build();
        }

        private SupportRequestResponse toResponse(
                        SupportRequest request,
                        UUID teamId) {

                return SupportRequestResponse.builder()
                                .id(request.getId())
                                .sosId(request.getSos().getId())
                                .status(request.getStatus())

                                .items(
                                                request.getItems()
                                                                .stream()
                                                                .filter(item -> teamId == null ||
                                                                                (item.getAssignedTeam() != null
                                                                                                && item.getAssignedTeam()
                                                                                                                .getId()
                                                                                                                .equals(teamId)))
                                                                .map(item -> SupportRequestItemResponse.builder()
                                                                                .id(item.getId())
                                                                                .supportType(item.getSupportType())
                                                                                .requiredGroupCount(item
                                                                                                .getRequiredGroupCount())
                                                                                .assignedGroupCount(item
                                                                                                .getAssignedGroupCount())
                                                                                .status(item.getStatus())
                                                                                .assignedTeamId(item
                                                                                                .getAssignedTeam() != null
                                                                                                                ? item.getAssignedTeam()
                                                                                                                                .getId()
                                                                                                                : null)
                                                                                .assignedTeamName(item
                                                                                                .getAssignedTeam() != null
                                                                                                                ? item.getAssignedTeam()
                                                                                                                                .getName()
                                                                                                                : null)
                                                                                .provinceNote(item.getProvinceNote())
                                                                                .teamResponse(item.getTeamResponse())
                                                                                .build())
                                                                .toList())

                                .reason(request.getReason())
                                .requestedById(request.getRequestedBy().getId())
                                .requestedByName(request.getRequestedBy().getHoten())
                                .approvedById(request.getApprovedBy() != null
                                                ? request.getApprovedBy().getId()
                                                : null)
                                .approvedByName(request.getApprovedBy() != null
                                                ? request.getApprovedBy().getHoten()
                                                : null)
                                .createdAt(request.getCreatedAt())
                                .reviewedAt(request.getReviewedAt())
                                .build();
        }

        private BigDecimal calculateDistanceKm(
                        BigDecimal lat1,
                        BigDecimal lon1,
                        BigDecimal lat2,
                        BigDecimal lon2) {

                if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                        return null;
                }

                final double EARTH_RADIUS = 6371.0;

                double lat1Value = lat1.doubleValue();
                double lon1Value = lon1.doubleValue();
                double lat2Value = lat2.doubleValue();
                double lon2Value = lon2.doubleValue();

                double dLat = Math.toRadians(lat2Value - lat1Value);
                double dLon = Math.toRadians(lon2Value - lon1Value);

                double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                + Math.cos(Math.toRadians(lat1Value))
                                                * Math.cos(Math.toRadians(lat2Value))
                                                * Math.sin(dLon / 2)
                                                * Math.sin(dLon / 2);

                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                double distance = EARTH_RADIUS * c;

                return BigDecimal.valueOf(distance)
                                .setScale(2, RoundingMode.HALF_UP);
        }
}
