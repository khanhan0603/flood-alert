package com.example.flood_alert.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.request.CreateProvinceOperatorRequest;
import com.example.flood_alert.dbo.request.DeleteProvinceOperatorRequest;
import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.ImportProvinceOperatorResponse;
import com.example.flood_alert.dbo.response.ProvinceOperatorDetailResponse;
import com.example.flood_alert.dbo.response.ProvinceOperatorResponse;
import com.example.flood_alert.dbo.response.RescueTeamSummaryResponse;
import com.example.flood_alert.service.ProvinceOperatorImportService;
import com.example.flood_alert.service.ProvinceOperatorService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.PutMapping;

import com.example.flood_alert.dbo.request.UpdateProvinceOperatorRequest;

@RestController
@RequestMapping("/province-operator")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProvinceOperatorController {
        ProvinceOperatorImportService provinceOperatorImportService;
        ProvinceOperatorService provinceOperatorService;

        @PostMapping("/import")
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<ImportProvinceOperatorResponse> importProvinceOperator(
                        @RequestParam("file") MultipartFile file)
                        throws IOException {
                return ApiResponse.<ImportProvinceOperatorResponse>builder()
                                .result(provinceOperatorImportService.importExcel(file))
                                .build();
        }

        // Danh sách các province
        @GetMapping
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<Page<ProvinceOperatorResponse>> getAll(
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse
                                .<Page<ProvinceOperatorResponse>>builder()
                                .result(provinceOperatorService.getAll(pageable))
                                .build();
        }

        @GetMapping("/search")
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<Page<ProvinceOperatorResponse>> search(
                        @RequestParam String keyword,
                        @PageableDefault(size = 20) Pageable pageable) {

                return ApiResponse.<Page<ProvinceOperatorResponse>>builder()
                                .result(provinceOperatorService.search(keyword, pageable))
                                .build();
        }

        // Chi tiết province
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<ProvinceOperatorDetailResponse> getDetail(
                        @PathVariable UUID id) {

                return ApiResponse
                                .<ProvinceOperatorDetailResponse>builder()
                                .result(provinceOperatorService.getDetail(id))
                                .build();
        }

        // Chi tiết đội
        @GetMapping("/{id}/teams")
        public ApiResponse<Page<RescueTeamSummaryResponse>> getTeams(
                        @PathVariable UUID id,
                        @PageableDefault(size = 10) Pageable pageable) {

                return ApiResponse
                                .<Page<RescueTeamSummaryResponse>>builder()
                                .result(
                                                provinceOperatorService
                                                                .getTeams(
                                                                                id,
                                                                                pageable))
                                .build();
        }

        @PostMapping
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<ProvinceOperatorResponse> create(
                        @Valid @RequestBody CreateProvinceOperatorRequest request) {

                return ApiResponse.<ProvinceOperatorResponse>builder()
                                .result(provinceOperatorService.create(request))
                                .build();
        }

        @DeleteMapping
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
        public ApiResponse<Void> deleteProvinceOperators(
                        @RequestBody @Valid DeleteProvinceOperatorRequest request) {

                provinceOperatorService.delete(request);

                return ApiResponse.<Void>builder()
                                .message("Xóa điều phối viên cấp tỉnh thành công.")
                                .build();
        }

        @PutMapping
        public ApiResponse<ProvinceOperatorDetailResponse> update(
                        @RequestBody @Valid UpdateProvinceOperatorRequest request) {
                return ApiResponse.<ProvinceOperatorDetailResponse>builder()
                                .result(provinceOperatorService.update(request))
                                .build();
        }
}
