package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.CustomerRefundDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.CustomerRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customer-refunds")
@RequiredArgsConstructor
public class CustomerRefundController {

    private final CustomerRefundService refundService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerRefundDto>>> getAllRefunds(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<CustomerRefundDto> list = refundService.getAllRefunds(
                search, searchBy, status, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success("Customer Refunds retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerRefundDto>> getRefundById(@PathVariable Long id) {
        CustomerRefundDto dto = refundService.getRefundById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer Refund retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerRefundDto>> createRefund(@RequestBody CustomerRefundDto dto) {
        CustomerRefundDto created = refundService.createRefund(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer Refund created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerRefundDto>> updateRefund(
            @PathVariable Long id,
            @RequestBody CustomerRefundDto dto
    ) {
        CustomerRefundDto updated = refundService.updateRefund(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Customer Refund updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomerRefundDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "NONE_VOID");
        CustomerRefundDto updated = refundService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRefund(@PathVariable Long id) {
        refundService.deleteRefund(id);
        return ResponseEntity.ok(ApiResponse.success("Customer Refund deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<String>> getNextCode() {
        String nextCode = refundService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next customer refund code generated", nextCode));
    }
}
