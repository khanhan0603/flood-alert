package com.example.flood_alert.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.ImportProvinceOperatorResponse;
import com.example.flood_alert.service.ProvinceOperatorImportService;


@RestController
@RequestMapping("/province-operator")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProvinceOperatorController {
    ProvinceOperatorImportService provinceOperatorImportService;
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ImportProvinceOperatorResponse> importProvinceOperator(@RequestParam("file") MultipartFile file) 
                throws IOException
    {
        return ApiResponse.<ImportProvinceOperatorResponse>builder()
                        .result(provinceOperatorImportService.importExcel(file))
                        .build();
    }
    
}
