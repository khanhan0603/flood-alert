package com.example.flood_alert.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flood_alert.service.AreaService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.flood_alert.dbo.response.AreaSimpleResponse;

import lombok.AccessLevel;
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
    
}
