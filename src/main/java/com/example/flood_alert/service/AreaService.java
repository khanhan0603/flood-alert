package com.example.flood_alert.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.response.AreaDataByParentResponse;
import com.example.flood_alert.dbo.response.AreaSimpleResponse;
import com.example.flood_alert.dbo.response.WardPolygonResponse;
import com.example.flood_alert.entity.Area;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.mapper.AreaMapper;
import com.example.flood_alert.repository.AreaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Transactional(readOnly = true) // Thêm dòng này — giữ session mở trong suốt transaction
public class AreaService {
    final AreaMapper areaMapper;
    final AreaRepository areaRepository;

    public List<AreaSimpleResponse> getAllAreaByTree() {
        List<Area> areas = areaRepository.findAllByOrderByLevelAsc();
        Map<UUID, AreaSimpleResponse> map = new HashMap<>();
        List<AreaSimpleResponse> roots = new ArrayList<>();

        for (Area area : areas) {
            AreaSimpleResponse response = areaMapper.toSimpleResponse(area);
            map.put(area.getId(), response);
        }

        for (Area area : areas) {
            AreaSimpleResponse current = map.get(area.getId());

            if (area.getParent() == null) {
                roots.add(current);
            } else {
                AreaSimpleResponse parent = map.get(area.getParent().getId()); // LAZY trigger ở đây
                if (parent != null) { // Thêm null check phòng data inconsistent
                    parent.getChildren().add(current);
                }
            }
        }

        return roots;
    }

    public List<AreaDataByParentResponse> getAreaByParentId(UUID parentId){
        List<AreaDataByParentResponse> areas=areaRepository.findByParentId(parentId);
        if(areas.isEmpty()){
            throw new AppException(ErrorCode.EMPTY_AREA_BY_PARENT_ID);
        }
        return areas;
    }

    public WardPolygonResponse findPolygonById(UUID id) throws Exception{
        Object result=areaRepository.findPolygonById(id);
        if(result==null){
            throw new AppException(ErrorCode.EMPTY_POLYGIN_BY_ID);
        }

        Object[] row=(Object[]) result;
        ObjectMapper mapper=new ObjectMapper();

        return WardPolygonResponse.builder()
                .id(UUID.fromString(row[0].toString()))
                .tenkhuvuc(row[1].toString())
                .geometry(
                    mapper.readValue((String) row[2], Object.class)
                )
                .build();
    }   
}