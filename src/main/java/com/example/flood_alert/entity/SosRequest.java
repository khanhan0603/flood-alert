package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.flood_alert.enums.Condition;
import com.example.flood_alert.enums.Priority;
import com.example.flood_alert.enums.StatusSOS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level=AccessLevel.PRIVATE)
@Entity
@Table(
    name="sos_requests"
)
public class SosRequest extends BaseEntity{
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=true)
    User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="area_id",nullable=false)
    Area area;

    String hoten;
    @Column(nullable=false)
    String sodt;
    String device_id;
    String ip_device;
    String location;
    @Column(columnDefinition="TEXT")
    String mota;
    @Column(nullable=false)
    LocalDateTime created_at;
    @Column(nullable=false)
    StatusSOS status;
    @Column(nullable=false)
    Priority priority;
    Condition condition;
    BigDecimal lat;
    BigDecimal lon;
    LocalDateTime updated_at;
}
