package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.SerialNumberDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.SerialNumberService;
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
 * Serial / batch tracking endpoints for the admin Stocks menu.
 * ROLE_ADMIN / ROLE_STORE is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/stocks/serials")
@RequiredArgsConstructor
public class SerialNumberController {

    private final SerialNumberService serialNumberService;

    /** List all serial entries, or filter by product: ?productId= */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SerialNumberDto>>> getAll(
            @RequestParam(required = false) Long productId) {
        List<SerialNumberDto> result = productId != null
                ? serialNumberService.getByProductId(productId)
                : serialNumberService.getAll();
        return ResponseEntity.ok(ApiResponse.success("Serial numbers retrieved successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SerialNumberDto>> create(@RequestBody SerialNumberDto dto) {
        SerialNumberDto created = serialNumberService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Serial number created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SerialNumberDto>> update(
            @PathVariable Long id, @RequestBody SerialNumberDto dto) {
        SerialNumberDto updated = serialNumberService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Serial number updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        serialNumberService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Serial number deleted successfully"));
    }
}
