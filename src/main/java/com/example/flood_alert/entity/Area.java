package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.util.List;

import org.locationtech.jts.geom.MultiPolygon;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name="areas")
public class Area extends BaseEntity{
    String tenkhuvuc;

    @Column(columnDefinition = "TEXT") //ép PostgreSQL dùng kiểu TEXT
    String mota;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Area parent;

    @JsonIgnore //Tránh loop vô hạn
    @OneToMany(mappedBy="parent")
    List<Area> children;
    
    @Column(columnDefinition="SMALLINT")
    int level;

    @Column(precision=10,scale=6)//Số thập phân lấy 6 phần thập phân
    BigDecimal lat;

    @Column(precision=10,scale=6)
    BigDecimal lon;

    @Column(columnDefinition="geometry(MultiPolygon,4326)")
    MultiPolygon polygon;

    @JsonIgnore
    @OneToMany(mappedBy="area")
    List<User> users;

    @JsonIgnore
    @OneToMany(mappedBy="area")
    List<WeatherData> weatherData;
}
