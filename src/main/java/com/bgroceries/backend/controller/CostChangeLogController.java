package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.CostChangeLogDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.CostChangeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cost change log endpoints for the admin Stocks menu.
 * ROLE_ADMIN / ROLE_STORE is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/stocks/cost-change-logs")
@RequiredArgsConstructor
public class CostChangeLogController {

    private final CostChangeLogService costChangeLogService;

    /** List all cost change log entries, or filter by product: ?productId= */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CostChangeLogDto>>> getAll(
            @RequestParam(required = false) Long productId) {
        List<CostChangeLogDto> result = productId != null
                ? costChangeLogService.getByProductId(productId)
                : costChangeLogService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Cost change logs retrieved successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CostChangeLogDto>> create(@RequestBody CostChangeLogDto dto) {
        CostChangeLogDto created = costChangeLogService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Cost change log created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CostChangeLogDto>> update(
            @PathVariable Long id, @RequestBody CostChangeLogDto dto) {
        CostChangeLogDto updated = costChangeLogService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cost change log updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        costChangeLogService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Cost change log deleted successfully"));
    }
}
