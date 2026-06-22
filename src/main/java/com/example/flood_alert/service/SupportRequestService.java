package com.example.flood_alert.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.response.SupportRequestResponse;
import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.RescueTeam;
import com.example.flood_alert.entity.SosRequest;
import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.SupportRequestStatus;
import com.example.flood_alert.enums.SupportType;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.SupportRequestMapper;
import com.example.flood_alert.repository.RescueGroupRepository;
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
    RescueGroupRepository groupRepository;
    UserRepository userRepository;
    SupportRequestMapper supportRequestMapper;

    // Tạo yêu cầu hỗ trợ cứu hộ, teamleader tạo
    public UUID create(UUID sosId, SupportType supportType, UUID suggestedGroupId, String reason, UUID currentUserId) {
        // Tìm thông tin người gửi yêu cầu
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tìm sos theo id
        SosRequest sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new AppException(ErrorCode.SOS_NOT_FOUND));

        // Nếu người dùng ko có team
        if (currentUser.getTeam() == null) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        // Lấy team theo user
        RescueTeam team = currentUser.getTeam();

        // Nếu team đó ko phải do người đó là leader
        if (!team.getLeader().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        // Tạo đối tượng group
        RescueGroup suggestedGroup = null;

        // Nếu không tìm thấy group theo id
        if (suggestedGroupId != null) {
            suggestedGroup = groupRepository.findById(suggestedGroupId)
                    .orElseThrow(() -> new AppException(ErrorCode.RESCUE_GROUP_NOT_FOUND));
        }

        // Tạo yêu cầu hỗ trợ
        SupportRequest supportRequest = SupportRequest.builder()
                .sos(sos)
                .requestedBy(currentUser)
                .status(SupportRequestStatus.PENDING)
                .supportType(supportType)
                .suggestedGroup(suggestedGroup)
                .reason(reason)
                .build();

        supportRequestRepository.save(supportRequest);
        return supportRequest.getId();
    }

    // Hiển thị danh sách các yêu cầu chi viện lên dashboard cho province leader
    @Transactional(readOnly = true)
    public List<SupportRequestResponse> getPendingRequests() {
        return supportRequestRepository.findByStatus(SupportRequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
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
                .requestedById(request.getRequestedBy().getId())
                .requestedByName(request.getRequestedBy().getHoten())
                .createdAt(request.getCreatedAt())
                .reviewedAt(request.getReviewedAt())
                .build();
    }
}
