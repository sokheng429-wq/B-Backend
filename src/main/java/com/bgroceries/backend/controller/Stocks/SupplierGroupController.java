package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.SupplierGroupDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.SupplierGroupService;
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
 * Admin supplier-group management endpoints (Stocks → Suppliers Group), all
 * wrapped in the standard {@link ApiResponse} envelope. ROLE_ADMIN is
 * enforced by the existing SecurityConfig {@code /api/admin/**} matcher — no
 * {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/supplier-groups")
@RequiredArgsConstructor
public class SupplierGroupController {

    private final SupplierGroupService supplierGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierGroupDto>>> getAllGroups() {
        List<SupplierGroupDto> groups = supplierGroupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success("Supplier groups retrieved successfully", groups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierGroupDto>> getGroupById(@PathVariable Long id) {
        SupplierGroupDto group = supplierGroupService.getGroupById(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier group retrieved successfully", group));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierGroupDto>> createGroup(@Valid @RequestBody SupplierGroupDto dto) {
        SupplierGroupDto created = supplierGroupService.createGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier group created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierGroupDto>> updateGroup(@PathVariable Long id, @Valid @RequestBody SupplierGroupDto dto) {
        SupplierGroupDto updated = supplierGroupService.updateGroup(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Supplier group updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        supplierGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier group deleted successfully"));
    }
}
