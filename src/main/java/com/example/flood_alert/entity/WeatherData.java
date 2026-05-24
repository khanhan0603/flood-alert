package com.example.flood_alert.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name="weather_datas",
        uniqueConstraints={
            @UniqueConstraint(name="uk_area_id_time",columnNames={"area_id","time"})
        })
public class WeatherData extends BaseEntity{
    @Column(precision=10,scale=2)
    BigDecimal rainfall;

    @Column(precision=10,scale=2)
    BigDecimal temperature;

    @Column(precision=10,scale=2)
    BigDecimal dewpoint;

    @Column(precision=10,scale=2)
    BigDecimal pressure;

    @Column(precision=10,scale=2)
    BigDecimal wind_speed;

    @Column(precision=10,scale=2)
    BigDecimal wind_direction;

    @Column(precision=10,scale=2)
    BigDecimal humidity;

    @Column(precision=10,scale=2)
    BigDecimal evapotranspiration;

    LocalDateTime time;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="area_id",nullable=false)
    Area area;
}
