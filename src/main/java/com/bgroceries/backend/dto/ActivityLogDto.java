package com.bgroceries.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogDto {
    private Long id;
    private String username;
    private String userRole;
    private String userFullName;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String description;
    private String icon;
    private String status;
    private String ipAddress;
    private LocalDateTime createdAt;
}