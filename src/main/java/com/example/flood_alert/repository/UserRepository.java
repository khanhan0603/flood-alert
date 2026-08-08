package com.example.flood_alert.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flood_alert.entity.Area;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.enums.Status;

public interface UserRepository extends JpaRepository<User, UUID> {
  boolean existsByRole(Role role);

  @Query("""
          SELECT COUNT(u) > 0
          FROM User u
          WHERE u.email = :email
            AND u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
      """)
  boolean existsActiveByEmail(String email);

  @Query("""
          SELECT COUNT(u) > 0
          FROM User u
          WHERE u.sodt = :sodt
            AND u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
      """)
  boolean existsActiveBySodt(String sodt);

  // Cho citizen update tài khoản
  boolean existsByEmail(String email);

  boolean existsBySodt(String sodt);

  @Query("""
          SELECT u
          FROM User u
          WHERE (u.email = :username OR u.sodt = :username)
            AND u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
      """)
  Optional<User> findActiveByEmailOrPhone(String username);

  @Query("""
          SELECT u
          FROM User u
          WHERE u.trangthai = :status
            AND u.sodt = :username
      """)
  Optional<User> findBySodtAndStatus(String username, Status status);

  Optional<User> findByEmail(String email);

  Optional<User> findBySodt(String sodt);

  List<User> findByAreaAndTrangthai(Area area, Status status);

  // Load toàn bộ email, sdt trong table
  @Query("""
          select u.email
          from User u
          where u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
      """)
  Set<String> findAllEmails();

  @Query("""
          select u.sodt
          from User u
          where u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
      """)
  Set<String> findAllPhones();

  // List các rescuer có team nhưng chưa thuộc group nào, ko hiển thị team leader
  @Query("""
      SELECT u
      FROM User u
      WHERE u.role = com.example.flood_alert.enums.Role.RESCUER
        AND u.team.id = :teamId
        AND (u.team.leader IS NULL OR u.id <> u.team.leader.id)
        AND NOT EXISTS (
              SELECT rgm
              FROM RescueGroupMember rgm
              WHERE rgm.user.id = u.id
        )
      """)
  List<User> findAvailableMembers(UUID teamId);

  // danh sách các province
  @Query("""
        SELECT u
        FROM User u
        WHERE u.role = :role
                AND u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
        ORDER BY u.id ASC
      """)
  Page<User> findByRole(Role role, Pageable pageable);

  // Danh sách điều hành cấp tỉnh phục vụ cho hàm notification
  @Query("""
        SELECT u
        FROM User u
        WHERE u.role = :role AND
              u.area.id = :areaId AND
              u.trangthai = :trangthai
        ORDER BY u.id ASC
      """)
  List<User> findByRoleAndArea_IdAndTrangthai(
      Role role,
      UUID areaId,
      Status trangthai);

  List<User> findAllByIdIn(Set<UUID> ids);

  // Tìm kiếm res theo keyword
  @Query("""
          SELECT u
          FROM User u
          WHERE u.role = com.example.flood_alert.enums.Role.RESCUER
            AND u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
            AND (
                  LOWER(u.hoten) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR u.sodt LIKE CONCAT('%', :keyword, '%')
            )
          ORDER BY u.hoten ASC
      """)
  List<User> searchActiveRescuers(String keyword);

  // Tìm kiếm pro theo keyword
  @Query("""
          SELECT u
          FROM User u
          WHERE u.role = com.example.flood_alert.enums.Role.PROVINCE_OPERATOR
            AND u.trangthai = com.example.flood_alert.enums.Status.ACTIVE
            AND (
                  LOWER(u.hoten) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR u.sodt LIKE CONCAT('%', :keyword, '%')
            )
          ORDER BY u.id ASC
      """)
  Page<User> searchActiveProvinceOperators(
      String keyword,
      Pageable pageable);
}
