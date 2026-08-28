package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.ProductSupplierLinkDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ProductSupplierLinkService;
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
 * Product-supplier link endpoints for the admin Stocks menu.
 * ROLE_ADMIN / ROLE_STORE is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/stocks/supplier-links")
@RequiredArgsConstructor
public class ProductSupplierLinkController {

    private final ProductSupplierLinkService productSupplierLinkService;

    /** List all links, or filter by product: ?productId= */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSupplierLinkDto>>> getAll(
            @RequestParam(required = false) Long productId) {
        List<ProductSupplierLinkDto> result = productId != null
                ? productSupplierLinkService.getByProductId(productId)
                : productSupplierLinkService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Supplier links retrieved successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSupplierLinkDto>> create(
            @RequestBody ProductSupplierLinkDto dto) {
        ProductSupplierLinkDto created = productSupplierLinkService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier link created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSupplierLinkDto>> update(
            @PathVariable Long id, @RequestBody ProductSupplierLinkDto dto) {
        ProductSupplierLinkDto updated = productSupplierLinkService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Supplier link updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productSupplierLinkService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier link deleted successfully"));
    }
}
