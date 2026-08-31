package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.ProductGroupDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ProductGroupService;
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
 * Admin product-group management endpoints (Stocks → Groups), all wrapped in
 * the standard {@link ApiResponse} envelope. ROLE_ADMIN is enforced by the
 * existing SecurityConfig {@code /api/admin/**} matcher — no
 * {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/product-groups")
@RequiredArgsConstructor
public class ProductGroupController {

    private final ProductGroupService productGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductGroupDto>>> getAllGroups() {
        List<ProductGroupDto> groups = productGroupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success("Product groups retrieved successfully", groups));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductGroupDto>> getGroupById(@PathVariable Long id) {
        ProductGroupDto group = productGroupService.getGroupById(id);
        return ResponseEntity.ok(ApiResponse.success("Product group retrieved successfully", group));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductGroupDto>> createGroup(@Valid @RequestBody ProductGroupDto dto) {
        ProductGroupDto created = productGroupService.createGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product group created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductGroupDto>> updateGroup(@PathVariable Long id, @Valid @RequestBody ProductGroupDto dto) {
        ProductGroupDto updated = productGroupService.updateGroup(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Product group updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable Long id) {
        productGroupService.deleteGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Product group deleted successfully"));
    }
}
