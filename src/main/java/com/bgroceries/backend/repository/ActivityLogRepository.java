package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findAllByOrderByCreatedAtDesc();

    List<ActivityLog> findTop100ByOrderByCreatedAtDesc();

    List<ActivityLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);

    List<ActivityLog> findByUsernameOrderByCreatedAtDesc(String username);

    List<ActivityLog> findByUserRoleOrderByCreatedAtDesc(String userRole);

    @Query("SELECT a FROM ActivityLog a WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " (a.userFullName IS NOT NULL AND LOWER(a.userFullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
           " (a.entityName IS NOT NULL AND LOWER(a.entityName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
           " LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:entityType IS NULL OR :entityType = '' OR a.entityType = :entityType) " +
           "AND (:actionType IS NULL OR :actionType = '' OR a.actionType = :actionType) " +
           "AND (:userRole IS NULL OR :userRole = '' OR a.userRole = :userRole) " +
           "ORDER BY a.createdAt DESC")
    List<ActivityLog> searchLogs(
            @Param("keyword") String keyword,
            @Param("entityType") String entityType,
            @Param("actionType") String actionType,
            @Param("userRole") String userRole
    );
}