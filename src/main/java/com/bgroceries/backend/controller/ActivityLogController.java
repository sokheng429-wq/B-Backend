package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.ActivityLogDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> getAllLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String userRole
    ) {
        List<ActivityLogDto> logs = activityLogService.getAllLogs(keyword, entityType, actionType, userRole);
        return ResponseEntity.ok(ApiResponse.success("Activity logs retrieved successfully", logs));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<ActivityLogDto>>> getRecentLogs(
            @RequestParam(defaultValue = "15") int limit
    ) {
        List<ActivityLogDto> logs = activityLogService.getRecentLogs(limit);
        return ResponseEntity.ok(ApiResponse.success("Recent activity logs retrieved successfully", logs));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActivityLogDto>> createLog(@RequestBody ActivityLogDto dto) {
        ActivityLogDto created = activityLogService.createLog(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Activity logged successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLog(@PathVariable Long id) {
        activityLogService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.success("Activity log deleted successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearAllLogs() {
        activityLogService.clearAllLogs();
        return ResponseEntity.ok(ApiResponse.success("All activity logs cleared successfully"));
    }
}