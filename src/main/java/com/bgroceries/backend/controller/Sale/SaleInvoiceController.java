package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.SaleInvoiceDto;
import com.bgroceries.backend.dto.SaleInvoicePaymentDto;
import com.bgroceries.backend.dto.SaleInvoiceStatsDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.SaleInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sale-invoices")
@RequiredArgsConstructor
public class SaleInvoiceController {

    private final SaleInvoiceService saleInvoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleInvoiceDto>>> getAllInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<SaleInvoiceDto> list = saleInvoiceService.getAllInvoices(search, searchBy, status, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Sale invoices retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleInvoiceDto>> getInvoiceById(@PathVariable Long id) {
        SaleInvoiceDto dto = saleInvoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Sale invoice retrieved successfully", dto));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<ApiResponse<SaleInvoiceDto>> getInvoiceByCode(@PathVariable String code) {
        SaleInvoiceDto dto = saleInvoiceService.getInvoiceByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Sale invoice retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SaleInvoiceDto>> createInvoice(@Valid @RequestBody SaleInvoiceDto dto) {
        SaleInvoiceDto created = saleInvoiceService.createInvoice(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sale invoice created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleInvoiceDto>> updateInvoice(@PathVariable Long id, @Valid @RequestBody SaleInvoiceDto dto) {
        SaleInvoiceDto updated = saleInvoiceService.updateInvoice(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Sale invoice updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        saleInvoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Sale invoice deleted successfully"));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<SaleInvoiceDto>> recordPayment(
            @PathVariable Long id,
            @RequestBody SaleInvoicePaymentDto paymentDto
    ) {
        SaleInvoiceDto updated = saleInvoiceService.recordPayment(id, paymentDto);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", updated));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String code = saleInvoiceService.generateNextInvoiceCode();
        return ResponseEntity.ok(ApiResponse.success("Next invoice code generated", Map.of("code", code)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SaleInvoiceStatsDto>> getStats() {
        SaleInvoiceStatsDto stats = saleInvoiceService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Invoice statistics retrieved successfully", stats));
    }
}