package com.example.flood_alert.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.flood_alert.entity.RescueGroup;
import com.example.flood_alert.entity.SosAssignment;
import com.example.flood_alert.entity.SupportRequest;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.AssignmentRole;
import com.example.flood_alert.enums.AssignmentStatus;
import com.example.flood_alert.enums.RescueGroupStatus;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.SupportRequestStatus;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.SosAssignmentRepository;
import com.example.flood_alert.repository.SupportRequestRepository;
import com.example.flood_alert.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProvinceDispatchService {
    SupportRequestRepository supportRequestRepository;
    RescueGroupRepository rescueGroupRepository;
    SosAssignmentRepository sosAssignmentRepository;
    UserRepository userRepository;

    //Province phê duyệt
    public void approve(UUID supportRequestId,UUID assignmentGroupId,String provinceResponse,UUID provinceUserId){
        //Tìm thông tin provice
        User provinceUser=userRepository.findById(provinceUserId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        //Kiểm tra xem user đó có phải role là PROVINCE ko
        if(provinceUser.getRole()!= Role.PROVINCE_OPERATOR){
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        //Tìm yêu cầu hỗ trợ theo id
        SupportRequest supportRequest=supportRequestRepository.findById(supportRequestId)
                    .orElseThrow(()-> new AppException(ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

        //Tìm thông tin group được giao nhiệm vụ theo id
        RescueGroup group=rescueGroupRepository.findById(assignmentGroupId)
                    .orElseThrow(()-> new AppException(ErrorCode.RESCUE_GROUP_NOT_FOUND));

        //Kiểm tra group đó có đang sẵn sàng ko
        if(group.getStatus()!=RescueGroupStatus.AVAILABLE){
            throw new AppException(ErrorCode.GROUP_NOT_AVAILABLE);
        }
        
        //Tạo thông tin giao nhiệm vụ sos
        SosAssignment assignment=SosAssignment.builder()
                    .sos(supportRequest.getSos())
                    .group(group)
                    .assignedBy(provinceUser)
                    .role(AssignmentRole.SUPPORT)
                    .status(AssignmentStatus.ASSIGNED)
                    .assignedAt(LocalDateTime.now())
                    .note("Province dispatch")
                    .build();

        sosAssignmentRepository.save(assignment);

        supportRequest.setStatus(SupportRequestStatus.APPROVED);
        //supportRequest.setAssignedGroup(group);
        supportRequest.setApprovedBy(provinceUser);
        supportRequest.setReviewedAt(LocalDateTime.now());
        supportRequest.setProvinceResponse(provinceResponse);

        supportRequestRepository.save(supportRequest);
    }

    //Từ chối yêu cầu hỗ trợ
    public void reject(UUID supprtRequestId,String provinceResponse,UUID provinceUserId){
        //Tìm thông tin provice
        User provinceUser=userRepository.findById(provinceUserId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        //Kiểm tra xem user đó có phải role là PROVINCE ko
        if(provinceUser.getRole()!= Role.PROVINCE_OPERATOR){
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        //Tìm yêu cầu hỗ trợ theo id
        SupportRequest supportRequest=supportRequestRepository.findById(supprtRequestId)
                    .orElseThrow(()-> new AppException(ErrorCode.SUPPORT_REQUEST_NOT_FOUND));

        supportRequest.setStatus(SupportRequestStatus.REJECTED);
        supportRequest.setApprovedBy(provinceUser);
        supportRequest.setReviewedAt(LocalDateTime.now());
        supportRequest.setProvinceResponse(provinceResponse);
        
        supportRequestRepository.save(supportRequest);
    }
}
