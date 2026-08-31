package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.StockDocumentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.entity.Stocks.StockDocument;
import com.bgroceries.backend.service.StockDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Stock ledger endpoints for the admin Stocks menu (Receive / Issue / Adjust
 * documents). ROLE_ADMIN is enforced by the existing SecurityConfig
 * {@code /api/admin/**} matcher — no {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/stock-documents")
@RequiredArgsConstructor
public class StockDocumentController {

    private final StockDocumentService stockDocumentService;

    /** List documents, optionally filtered: ?docType=RECEIVE|ISSUE|ADJUST. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDocumentDto>>> getAll(
            @RequestParam(required = false) StockDocument.DocType docType) {
        List<StockDocumentDto> docs = stockDocumentService.getAll(docType);
        return ResponseEntity.ok(ApiResponse.success("Stock documents retrieved successfully", docs));
    }

    /** Full movement history of one product across all document types. */
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<StockDocumentDto>>> getByProduct(@PathVariable Long productId) {
        List<StockDocumentDto> docs = stockDocumentService.getByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product stock history retrieved successfully", docs));
    }

    /** Post a new document; product on-hand/average-cost are updated in the same transaction. */
    @PostMapping
    public ResponseEntity<ApiResponse<StockDocumentDto>> create(@Valid @RequestBody StockDocumentDto dto) {
        StockDocumentDto created = stockDocumentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock document created successfully", created));
    }

    /**
     * Delete a document AND reverse its stock effect — receive gives quantity
     * back, issue returns issued units, adjust restores the before-count.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        stockDocumentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Stock document deleted successfully"));
    }
}
