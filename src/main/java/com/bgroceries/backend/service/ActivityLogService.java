package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ActivityLogDto;
import com.bgroceries.backend.entity.ActivityLog;
import com.bgroceries.backend.repository.ActivityLogRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional(readOnly = true)
    public List<ActivityLogDto> getAllLogs(String keyword, String entityType, String actionType, String userRole) {
        List<ActivityLog> list;
        if ((keyword != null && !keyword.isBlank()) ||
            (entityType != null && !entityType.isBlank()) ||
            (actionType != null && !actionType.isBlank()) ||
            (userRole != null && !userRole.isBlank())) {
            list = activityLogRepository.searchLogs(
                    keyword != null && !keyword.isBlank() ? keyword.trim() : null,
                    entityType != null && !entityType.isBlank() ? entityType.trim() : null,
                    actionType != null && !actionType.isBlank() ? actionType.trim() : null,
                    userRole != null && !userRole.isBlank() ? userRole.trim() : null
            );
        } else {
            list = activityLogRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDto> getRecentLogs(int limit) {
        return activityLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .limit(limit > 0 ? limit : 50)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityLogDto createLog(ActivityLogDto dto) {
        ActivityLog entity = ActivityLog.builder()
                .username(dto.getUsername() != null ? dto.getUsername() : "admin")
                .userRole(dto.getUserRole() != null ? dto.getUserRole() : "ADMIN")
                .userFullName(dto.getUserFullName())
                .actionType(dto.getActionType() != null ? dto.getActionType().toUpperCase() : "CREATE")
                .entityType(dto.getEntityType() != null ? dto.getEntityType().toUpperCase() : "PRODUCT")
                .entityId(dto.getEntityId())
                .entityName(dto.getEntityName())
                .description(dto.getDescription() != null ? dto.getDescription() : "Action performed in admin panel")
                .icon(dto.getIcon() != null ? dto.getIcon() : "📦")
                .status(dto.getStatus() != null ? dto.getStatus() : "SUCCESS")
                .ipAddress(dto.getIpAddress())
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .build();

        ActivityLog saved = activityLogRepository.save(entity);
        log.info("Activity logged: [{}] {} by {} ({})", saved.getActionType(), saved.getEntityName(), saved.getUsername(), saved.getUserRole());
        return toDto(saved);
    }

    @Transactional
    public void log(String username, String userRole, String userFullName,
                    String actionType, String entityType, Long entityId,
                    String entityName, String description, String icon, String status) {
        createLog(ActivityLogDto.builder()
                .username(username)
                .userRole(userRole)
                .userFullName(userFullName)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .description(description)
                .icon(icon)
                .status(status)
                .build());
    }

    @Transactional
    public void deleteLog(Long id) {
        activityLogRepository.deleteById(id);
    }

    @Transactional
    public void clearAllLogs() {
        activityLogRepository.deleteAll();
    }

    private ActivityLogDto toDto(ActivityLog entity) {
        return ActivityLogDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .userRole(entity.getUserRole())
                .userFullName(entity.getUserFullName())
                .actionType(entity.getActionType())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .entityName(entity.getEntityName())
                .description(entity.getDescription())
                .icon(entity.getIcon())
                .status(entity.getStatus())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @PostConstruct
    public void seedInitialLogsIfEmpty() {
        if (activityLogRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();
            List<ActivityLog> seeds = List.of(
                    ActivityLog.builder()
                            .username("admin")
                            .userRole("ADMIN")
                            .userFullName("Administrator")
                            .actionType("CREATE")
                            .entityType("PRODUCT")
                            .entityName("Organic Dragon Fruit (1kg)")
                            .description("Added new grocery product SKU to catalog with price $3.50")
                            .icon("📦")
                            .status("SUCCESS")
                            .createdAt(now.minusHours(2))
                            .build(),
                    ActivityLog.builder()
                            .username("store_mgr")
                            .userRole("STORE")
                            .userFullName("Store Manager")
                            .actionType("TRANSFER")
                            .entityType("TRANSFER")
                            .entityName("TR-202608-001")
                            .description("Created stock transfer request from Central Warehouse to Store #1")
                            .icon("🔄")
                            .status("SUCCESS")
                            .createdAt(now.minusHours(5))
                            .build(),
                    ActivityLog.builder()
                            .username("admin")
                            .userRole("ADMIN")
                            .userFullName("Administrator")
                            .actionType("CREATE")
                            .entityType("JOB")
                            .entityName("Senior Store Supervisor")
                            .description("Published new career opening in Retail Management department")
                            .icon("💼")
                            .status("SUCCESS")
                            .createdAt(now.minusDays(1))
                            .build(),
                    ActivityLog.builder()
                            .username("admin")
                            .userRole("ADMIN")
                            .userFullName("Administrator")
                            .actionType("CREATE")
                            .entityType("PROMOTION")
                            .entityName("Weekend Fresh Discount - 15% OFF")
                            .description("Configured promotion code FRESH15 for all fruits & vegetables")
                            .icon("🏷️")
                            .status("SUCCESS")
                            .createdAt(now.minusDays(2))
                            .build(),
                    ActivityLog.builder()
                            .username("store_mgr")
                            .userRole("STORE")
                            .userFullName("Store Manager")
                            .actionType("UPDATE")
                            .entityType("PRODUCT")
                            .entityName("Fresh Milk 1L")
                            .description("Updated on-hand stock quantity from 12 to 50 units")
                            .icon("📦")
                            .status("SUCCESS")
                            .createdAt(now.minusDays(3))
                            .build()
            );
            activityLogRepository.saveAll(seeds);
            log.info("Seeded {} initial activity logs", seeds.size());
        }
    }
}