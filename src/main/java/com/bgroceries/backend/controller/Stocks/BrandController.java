package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.BrandDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.BrandService;
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
 * Admin brand management endpoints (Stocks → Brands), all wrapped in the
 * standard {@link ApiResponse} envelope. ROLE_ADMIN is enforced by the
 * existing SecurityConfig {@code /api/admin/**} matcher — no
 * {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandDto>>> getAllBrands() {
        List<BrandDto> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved successfully", brands));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandDto>> getBrandById(@PathVariable Long id) {
        BrandDto brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success("Brand retrieved successfully", brand));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandDto>> createBrand(@Valid @RequestBody BrandDto dto) {
        BrandDto created = brandService.createBrand(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Brand created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandDto>> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandDto dto) {
        BrandDto updated = brandService.updateBrand(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Brand updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success("Brand deleted successfully"));
    }
}
