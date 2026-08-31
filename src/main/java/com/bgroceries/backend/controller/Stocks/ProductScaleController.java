package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.ProductScaleDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ProductScaleService;
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
 * PLU / weigh-scale configuration endpoints for the admin Stocks menu.
 * ROLE_ADMIN / ROLE_STORE is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/stocks/scales")
@RequiredArgsConstructor
public class ProductScaleController {

    private final ProductScaleService productScaleService;

    /** List all scale entries, or filter by product: ?productId= */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductScaleDto>>> getAll(
            @RequestParam(required = false) Long productId) {
        List<ProductScaleDto> result = productId != null
                ? productScaleService.getByProductId(productId)
                : productScaleService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Product scales retrieved successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductScaleDto>> create(@RequestBody ProductScaleDto dto) {
        ProductScaleDto created = productScaleService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product scale created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductScaleDto>> update(
            @PathVariable Long id, @RequestBody ProductScaleDto dto) {
        ProductScaleDto updated = productScaleService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Product scale updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productScaleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Product scale deleted successfully"));
    }
}
