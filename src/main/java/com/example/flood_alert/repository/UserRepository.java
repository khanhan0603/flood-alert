package com.example.flood_alert.repository;


import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;


public interface UserRepository extends JpaRepository<User,UUID> {
    boolean existsByRole(Role role);
    boolean existsByEmailOrSodt(String email,String sodt);
}
