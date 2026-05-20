package com.example.flood_alert.entity;

import java.util.UUID;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter 
@Setter 
@RequiredArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
@MappedSuperclass
public class BaseEntity {
    @Id
    UUID id;
    @PrePersist
    public void prePersist() {
        if (id == null) { 
            id = Generators .timeBasedEpochGenerator() .generate(); 
        }
    }
    
}
