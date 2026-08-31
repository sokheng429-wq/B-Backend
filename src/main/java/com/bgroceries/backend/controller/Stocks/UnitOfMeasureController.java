package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.UnitOfMeasureDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.UnitOfMeasureService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin unit-of-measure management endpoints (Stocks → Unit of Measure), all
 * wrapped in the standard {@link ApiResponse} envelope. ROLE_ADMIN is
 * enforced by the existing SecurityConfig {@code /api/admin/**} matcher — no
 * {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/unit-of-measures")
@RequiredArgsConstructor
public class UnitOfMeasureController {

    private final UnitOfMeasureService unitOfMeasureService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UnitOfMeasureDto>>> getAllUnits() {
        List<UnitOfMeasureDto> units = unitOfMeasureService.getAllUnits();
        return ResponseEntity.ok(ApiResponse.success("Units retrieved successfully", units));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitOfMeasureDto>> getUnitById(@PathVariable Long id) {
        UnitOfMeasureDto unit = unitOfMeasureService.getUnitById(id);
        return ResponseEntity.ok(ApiResponse.success("Unit retrieved successfully", unit));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UnitOfMeasureDto>> createUnit(@Valid @RequestBody UnitOfMeasureDto dto) {
        UnitOfMeasureDto created = unitOfMeasureService.createUnit(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Unit created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UnitOfMeasureDto>> updateUnit(@PathVariable Long id, @Valid @RequestBody UnitOfMeasureDto dto) {
        UnitOfMeasureDto updated = unitOfMeasureService.updateUnit(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Unit updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable Long id) {
        unitOfMeasureService.deleteUnit(id);
        return ResponseEntity.ok(ApiResponse.success("Unit deleted successfully"));
    }
}
