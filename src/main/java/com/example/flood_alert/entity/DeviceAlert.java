package com.example.flood_alert.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@Entity
@Table(name="device_alert")
public class DeviceAlert extends BaseEntity{
    @ManyToOne
    IoTDevice iotDevice;
    
    String message;

    Boolean resolved;

    LocalDateTime createdAt;
}
