package com.example.flood_alert.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.request.AnonymousSosDetailRequest;
import com.example.flood_alert.dbo.request.AnonymousSosListRequest;
import com.example.flood_alert.dbo.request.CancelAnonymousSosRequest;
import com.example.flood_alert.dbo.request.CreateSosRequest;
import com.example.flood_alert.dbo.request.SearchSosRequest;
import com.example.flood_alert.dbo.request.UpdateAnonymousSosRequest;
import com.example.flood_alert.dbo.request.UpdateSosRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.CitizenSosDetailResponse;
import com.example.flood_alert.dbo.response.SosDetailResponse;
import com.example.flood_alert.dbo.response.SosResponse;
import com.example.flood_alert.dbo.response.TeamDashboardResponse;
import com.example.flood_alert.enums.StatusSOS;
import com.example.flood_alert.service.SOSRequestService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sos-request")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SosRequestController {
        SOSRequestService sosRequestService;

        @PostMapping
        public ApiResponse<SosResponse> create(
                        @RequestBody CreateSosRequest request,
                        HttpServletRequest httpRequest) {
                System.out.println("========== CREATE ==========");
                return ApiResponse.<SosResponse>builder()
                                .result(sosRequestService.create(request, httpRequest))
                                .build();
        }

        // Update cho người dân đã có tài khoản
        @PutMapping("/{sosId}")
        @PreAuthorize("hasAuthority('SCOPE_CITIZEN')")
        public ApiResponse<SosResponse> update(
                        @PathVariable UUID sosId,
                        @RequestBody @Valid UpdateSosRequest request,
                        HttpServletRequest httpRequest) {

                return ApiResponse.<SosResponse>builder()
                                .result(sosRequestService.update(
                                                sosId,
                                                request,
                                                httpRequest))
                                .build();
        }

        // Update cho người dân không có tài khoản
        @PutMapping("/{sosId}/anonymous")
        public ApiResponse<SosResponse> updateAnonymous(
                        @PathVariable UUID sosId,
                        @RequestBody @Valid UpdateAnonymousSosRequest request,
                        HttpServletRequest httpRequest) {

                return ApiResponse.<SosResponse>builder()
                                .result(
                                                sosRequestService.updateAnonymous(
                                                                sosId,
                                                                request,
                                                                httpRequest))
                                .build();
        }

        // Người dân xem chi tiết SOS của mình
        @GetMapping("/my/{sosId}")
        @PreAuthorize("hasAuthority('SCOPE_CITIZEN')")
        public ApiResponse<CitizenSosDetailResponse> getMySosDetail(
                        @PathVariable UUID sosId) {

                return ApiResponse.<CitizenSosDetailResponse>builder()
                                .result(sosRequestService.getMySosDetail(sosId))
                                .build();
        }

        // Người gửi SOS ẩn danh xem chi tiết SOS
        @PostMapping("/anonymous/{sosId}")
        public ApiResponse<CitizenSosDetailResponse> getAnonymousSosDetail(
                        @PathVariable UUID sosId,
                        @RequestBody AnonymousSosDetailRequest request) {

                return ApiResponse.<CitizenSosDetailResponse>builder()
                                .result(sosRequestService.getAnonymousSosDetail(
                                                sosId,
                                                request.getSodt(),
                                                request.getClientDeviceId()))
                                .build();
        }

        // List sos request xếp theo status
        @GetMapping("/my-sos")
        @PreAuthorize("hasAuthority('SCOPE_CITIZEN')")
        public ApiResponse<Page<SosResponse>> getMySos(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(
                                                sosRequestService.getMySos(
                                                                pageable))
                                .build();
        }

        // List danh sách yêu cầu đang hoạt động cho người lạ
        @PostMapping("/my-active-anonymous")
        public ApiResponse<Page<SosResponse>> getAnonymousActiveSos(

                        @Valid @RequestBody AnonymousSosListRequest request,

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "10") int size) {

                Pageable pageable = PageRequest.of(page, size);

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(
                                                sosRequestService.getAnonymousActiveSos(
                                                                request,
                                                                pageable))
                                .build();
        }

        // Dashboard cho team leader
        @GetMapping("/{teamId}/team/summary")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<TeamDashboardResponse> getMyDashboard(@PathVariable UUID teamId) {

                return ApiResponse
                                .<TeamDashboardResponse>builder()
                                .result(
                                                sosRequestService.getTeamDashboard(teamId))
                                .build();
        }

        // Danh sách các sos thuộc từng trạng thái của team
        @GetMapping("/team/{status}")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Page<SosResponse>> getMyTeamSosByStatus(
                        @PathVariable StatusSOS status,
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(sosRequestService.getMyTeamSosByStatus(status, pageable))
                                .build();
        }

        // Danh sách các sos của team
        @GetMapping("/team")
        public ApiResponse<Page<SosResponse>> getMyTeamSos(
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<SosResponse>>builder()
                                .result(
                                                sosRequestService.getMyTeamSos(pageable))
                                .build();
        }

        // Chi tiết sos
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('SCOPE_RESCUER') or hasAuthority('SCOPE_PROVINCE_OPERATOR')")
        public ApiResponse<SosDetailResponse> getDetail(
                        @PathVariable UUID id) {

                return ApiResponse.<SosDetailResponse>builder()
                                .result(
                                                sosRequestService.getDetail(id))
                                .build();
        }

        // Cancel sos người ẩn danh
        @PatchMapping("/{sosId}/anonymous/cancel")
        public ApiResponse<Void> cancelAnonymous(
                        @PathVariable UUID sosId,
                        @Valid @RequestBody CancelAnonymousSosRequest request) {

                sosRequestService.cancelAnonymous(
                                sosId,
                                request.getSodt(),
                                request.getClientDeviceId());

                return ApiResponse.<Void>builder()
                                .message("Hủy yêu cầu cứu hộ thành công.")
                                .build();
        }

        // Cancel người có tài khoản
        @PatchMapping("/{sosId}/cancel")
        @PreAuthorize("hasAuthority('SCOPE_CITIZEN')")
        public ApiResponse<Void> cancel(
                        @PathVariable UUID sosId) {

                sosRequestService.cancel(sosId);

                return ApiResponse.<Void>builder()
                                .message("Hủy yêu cầu cứu hộ thành công.")
                                .build();
        }

        /**
         * Tra cứu thông tin SOS bằng mã tracking.
         * Người dân không cần đăng nhập.
         */
        @GetMapping("/search")
        public ApiResponse<Page<SosResponse>> getByTrackingCode(
                        SearchSosRequest request,
                        @PageableDefault(size = 5) Pageable pageable) {

                return ApiResponse. <Page<SosResponse>>builder()
                                .result(sosRequestService.searchCitizenSos(request, pageable))
                                .build();
        }

        // Nhận điều phối sau call workflow thất bại
        @PutMapping("/{sosId}/claim-dispatcher")
        @PreAuthorize("hasAuthority('SCOPE_PROVINCE_OPERATOR')or hasAuthority('SCOPE_RESCUER')")
        public ApiResponse<Void> claimDispatcher(
                        @PathVariable UUID sosId) {

                sosRequestService.claimDispatcher(sosId);

                return ApiResponse.<Void>builder().build();
        }
}
