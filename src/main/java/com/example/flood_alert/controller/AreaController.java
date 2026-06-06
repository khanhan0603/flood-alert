package com.example.flood_alert.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.AreaService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

import com.example.flood_alert.dbo.response.AreaSimpleResponse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.RequestParam;

import com.example.flood_alert.dbo.response.AreaDataByParentResponse;
import com.example.flood_alert.dbo.response.WardPolygonResponse;



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
    
    
}
