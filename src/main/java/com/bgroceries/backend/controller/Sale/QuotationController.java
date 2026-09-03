package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.QuotationDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuotationDto>>> getAllQuotations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String salesperson,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<QuotationDto> list = quotationService.getAllQuotations(
                search, searchBy, status, outlet, customer, salesperson, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success("Quotations retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationDto>> getQuotationById(@PathVariable Long id) {
        QuotationDto dto = quotationService.getQuotationById(id);
        return ResponseEntity.ok(ApiResponse.success("Quotation retrieved successfully", dto));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<ApiResponse<QuotationDto>> getQuotationByCode(@PathVariable String code) {
        QuotationDto dto = quotationService.getQuotationByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Quotation retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuotationDto>> createQuotation(@RequestBody QuotationDto dto) {
        QuotationDto created = quotationService.createQuotation(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuotationDto>> updateQuotation(
            @PathVariable Long id,
            @RequestBody QuotationDto dto
    ) {
        QuotationDto updated = quotationService.updateQuotation(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Quotation updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<QuotationDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        QuotationDto updated = quotationService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Quotation status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuotation(@PathVariable Long id) {
        quotationService.deleteQuotation(id);
        return ResponseEntity.ok(ApiResponse.success("Quotation deleted successfully"));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String nextCode = quotationService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next quotation code generated", Map.of("code", nextCode)));
    }
}
