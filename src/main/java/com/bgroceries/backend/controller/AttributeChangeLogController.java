package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.AttributeChangeLogDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.AttributeChangeLogService;
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
 * Attribute change log endpoints for the admin Stocks menu.
 * ROLE_ADMIN / ROLE_STORE is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/stocks/attribute-change-logs")
@RequiredArgsConstructor
public class AttributeChangeLogController {

    private final AttributeChangeLogService attributeChangeLogService;

    /** List all attribute change log entries, or filter by product: ?productId= */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttributeChangeLogDto>>> getAll(
            @RequestParam(required = false) Long productId) {
        List<AttributeChangeLogDto> result = productId != null
                ? attributeChangeLogService.getByProductId(productId)
                : attributeChangeLogService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Attribute change logs retrieved successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttributeChangeLogDto>> create(
            @RequestBody AttributeChangeLogDto dto) {
        AttributeChangeLogDto created = attributeChangeLogService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attribute change log created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttributeChangeLogDto>> update(
            @PathVariable Long id, @RequestBody AttributeChangeLogDto dto) {
        AttributeChangeLogDto updated = attributeChangeLogService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Attribute change log updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        attributeChangeLogService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Attribute change log deleted successfully"));
    }
}
