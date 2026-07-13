package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.CreateHotlineSosRequest;
import com.example.flood_alert.dbo.request.EmergencyContactRequest;
import com.example.flood_alert.dbo.request.SearchHotlineSosRequest;
import com.example.flood_alert.dbo.request.UpdateHotlineSosRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.CallEventResponse;
import com.example.flood_alert.dbo.response.CreateHotlineSosResponse;
import com.example.flood_alert.dbo.response.EmergencyContactResponse;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.dbo.response.StatusOptionResponse;
import com.example.flood_alert.enums.CallEventStatus;
import com.example.flood_alert.service.HotlineService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/hotline")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HotlineController {

        HotlineService hotlineService;

        // Lấy emergency của team gửi đến người dân đồng thời tạo
        // EmergencyContactRequest
        // Trả số điện thoại liên hệ của đội gần nhất
        @PostMapping("/emergency-contact")
        public ApiResponse<EmergencyContactResponse> getEmergencyContact(
                        @RequestBody @Valid EmergencyContactRequest request) {

                return ApiResponse.<EmergencyContactResponse>builder()
                                .result(hotlineService.createEmergencyCall(request))
                                .build();
        }

        /**
         * Operator tạo SOS thay người dân sau khi tiếp nhận cuộc gọi Hotline.
         */
        @PostMapping("/sos")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<CreateHotlineSosResponse> createHotlineSos(
                        @RequestBody @Valid CreateHotlineSosRequest request) {

                return ApiResponse.<CreateHotlineSosResponse>builder()
                                .result(hotlineService.createHotlineSos(request))
                                .build();
        }

        // Danh sách cuộc gọi chờ, chỉ xem cuộc gọi đang chờ.
        @GetMapping("/call-events")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<CallEventResponse>> getPendingCallEvents(

                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<CallEventResponse>>builder()
                                .result(hotlineService.getPendingCallEvents(pageable))
                                .build();
        }

        /**
         * Xem chi tiết một cuộc gọi Hotline.
         */
        @GetMapping("/call-events/{id}")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<CallEventResponse> getCallEvent(
                        @PathVariable UUID id) {

                return ApiResponse.<CallEventResponse>builder()
                                .result(hotlineService.getCallEvent(id))
                                .build();
        }

        // Danh sách lịch sử để tra cứu, chỉ xem lịch sử (MATCHED, STALE).
        @GetMapping("/history")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<CallEventResponse>> getHistory(

                        @RequestParam CallEventStatus status,

                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<CallEventResponse>>builder()
                                .result(hotlineService.getHistory(
                                                status,
                                                pageable))
                                .build();
        }

        /**
         * Tìm kiếm SOS dành cho Operator Hotline.
         * Keyword có thể là:
         * - Số điện thoại
         * - Mã tracking
         */
        @GetMapping("/sos/search")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<SosResponse>> searchHotlineSos(

                        SearchHotlineSosRequest request,

                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(hotlineService.searchHotlineSos(
                                                request,
                                                pageable))
                                .build();
        }

        /**
         * Danh sách trạng thái SOS.
         * Dùng để hiển thị ComboBox trên giao diện.
         */
        @GetMapping("/status-options")
        public ApiResponse<List<StatusOptionResponse>> getStatusOptions() {

                return ApiResponse.<List<StatusOptionResponse>>builder()
                                .result(hotlineService.getStatusOptions())
                                .build();
        }

        /**
         * Danh sách các SOS do Operator nhập tay.
         */
        @GetMapping("/manual-sos")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<SosResponse>> getManualHotlineSos(

                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(hotlineService.getManualHotlineSos(pageable))
                                .build();
        }

        /**
         * Hotline cập nhật thông tin SOS.
         */
        @PutMapping("/sos/{id}")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<SosResponse> updateHotlineSos(
                        @PathVariable UUID id,
                        @RequestBody @Valid UpdateHotlineSosRequest request) {

                return ApiResponse.<SosResponse>builder()
                                .result(hotlineService.updateHotlineSos(id, request))
                                .build();
        }
}