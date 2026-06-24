package com.example.flood_alert.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.ImportProvinceOperatorResponse;
import com.example.flood_alert.dbo.response.ProvinceOperatorDetailResponse;
import com.example.flood_alert.dbo.response.ProvinceOperatorResponse;
import com.example.flood_alert.dbo.response.RescueTeamSummaryResponse;
import com.example.flood_alert.service.ProvinceOperatorImportService;
import com.example.flood_alert.service.ProvinceOperatorService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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
        @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
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
}
