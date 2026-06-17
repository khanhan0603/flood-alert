package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.dbo.response.ApiResponse;
import com.example.flood_alert.dbo.response.AreaDataByParentResponse;
import com.example.flood_alert.dbo.response.AreaDetailResponse;
import com.example.flood_alert.dbo.response.AreaSimpleResponse;
import com.example.flood_alert.dbo.response.WardPolygonResponse;
import com.example.flood_alert.service.AreaService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;



@RestController
@RequestMapping("/area")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaController {
    final AreaService areaService;

    @GetMapping("/list")
    public List<AreaSimpleResponse> getList() {
        return areaService.getAllAreaByTree();
    }
    @GetMapping("/list-by-parent")
    public List<AreaDataByParentResponse> getAreaBtParentId(@RequestParam UUID parentId) {
        return areaService.getAreaByParentId(parentId);
    }

    @GetMapping("/polygon-by-id")
    public WardPolygonResponse findPolygonById(@RequestParam UUID id) throws Exception{
        return areaService.findPolygonById(id);
    }
    


    @GetMapping("/search")
    public Page<AreaSimpleResponse> searchArea(@RequestParam String keyword) {
        return areaService.searchArea(keyword, PageRequest.of(0,10));
    }
    
    @GetMapping("/detail/{areaId}")
    public ApiResponse<AreaDetailResponse> getDetailArea(@PathVariable UUID areaId) {
        return ApiResponse.<AreaDetailResponse>builder()
                            .result(areaService.getDetailArea(areaId))
                            .build();
    }
    
}
