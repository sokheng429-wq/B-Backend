package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.AdjustmentDocumentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.AdjustmentDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/adjustment-documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdjustmentDocumentController {

    private final AdjustmentDocumentService adjService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdjustmentDocumentDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Adjustment documents retrieved", adjService.getAll()));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<AdjustmentDocumentDto>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Product adjustment history retrieved", adjService.getByProductId(productId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdjustmentDocumentDto>> create(@Valid @RequestBody AdjustmentDocumentDto dto) {
        AdjustmentDocumentDto created = adjService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Adjustment document created successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        adjService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Adjustment document deleted successfully"));
    }
}