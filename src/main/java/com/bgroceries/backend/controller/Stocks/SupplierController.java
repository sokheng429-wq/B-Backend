package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.SupplierDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.SupplierService;
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
 * Admin supplier management endpoints (Stocks → Suppliers), all wrapped in
 * the standard {@link ApiResponse} envelope. ROLE_ADMIN is enforced by the
 * existing SecurityConfig {@code /api/admin/**} matcher — no
 * {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDto>>> getAllSuppliers() {
        List<SupplierDto> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success("Suppliers retrieved successfully", suppliers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> getSupplierById(@PathVariable Long id) {
        SupplierDto supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier retrieved successfully", supplier));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SupplierDto>> createSupplier(@Valid @RequestBody SupplierDto dto) {
        SupplierDto created = supplierService.createSupplier(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Supplier created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDto dto) {
        SupplierDto updated = supplierService.updateSupplier(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Supplier updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Supplier deleted successfully"));
    }
}
