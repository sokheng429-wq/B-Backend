package com.bgroceries.backend.controller.Purchase;

import com.bgroceries.backend.dto.ReceiptPODto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ReceiptPOService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/receipt-pos")
@RequiredArgsConstructor
public class ReceiptPOController {

    private final ReceiptPOService receiptPOService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReceiptPODto>>> getAllReceiptPOs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) String status
    ) {
        List<ReceiptPODto> list = receiptPOService.getAllReceiptPOs(search, searchBy, fromDate, toDate, outlet, status);
        return ResponseEntity.ok(ApiResponse.success("Receipt POs retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptPODto>> getReceiptPOById(@PathVariable Long id) {
        ReceiptPODto dto = receiptPOService.getReceiptPOById(id);
        return ResponseEntity.ok(ApiResponse.success("Receipt PO retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReceiptPODto>> createReceiptPO(@RequestBody ReceiptPODto dto) {
        ReceiptPODto created = receiptPOService.createReceiptPO(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receipt PO created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReceiptPODto>> updateReceiptPO(
            @PathVariable Long id,
            @RequestBody ReceiptPODto dto
    ) {
        ReceiptPODto updated = receiptPOService.updateReceiptPO(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Receipt PO updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReceiptPODto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required"));
        }
        ReceiptPODto updated = receiptPOService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Receipt PO status updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReceiptPO(@PathVariable Long id) {
        receiptPOService.deleteReceiptPO(id);
        return ResponseEntity.ok(ApiResponse.success("Receipt PO deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String code = receiptPOService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next Receipt PO code generated", Map.of("code", code)));
    }
}
