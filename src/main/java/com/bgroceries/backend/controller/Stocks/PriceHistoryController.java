package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.PriceHistoryDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.PriceHistoryService;
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
 * Price history endpoints for the admin Stocks menu.
 * ROLE_ADMIN / ROLE_STORE is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/stocks/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    /** List all price history entries, or filter by product: ?productId= */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PriceHistoryDto>>> getAll(
            @RequestParam(required = false) Long productId) {
        List<PriceHistoryDto> result = productId != null
                ? priceHistoryService.getByProductId(productId)
                : priceHistoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Price history retrieved successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PriceHistoryDto>> create(@RequestBody PriceHistoryDto dto) {
        PriceHistoryDto created = priceHistoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Price history entry created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PriceHistoryDto>> update(
            @PathVariable Long id, @RequestBody PriceHistoryDto dto) {
        PriceHistoryDto updated = priceHistoryService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Price history entry updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        priceHistoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Price history entry deleted successfully"));
    }
}
