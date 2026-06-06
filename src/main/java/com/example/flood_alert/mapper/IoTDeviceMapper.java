package com.example.flood_alert.mapper;

import org.mapstruct.Mapper;

import com.example.flood_alert.entity.IoTDevice;

@Mapper(componentModel="spring")
public interface IoTDeviceMapper {
    IoTDevice toIoTDevice(IoTDevice request);
}
