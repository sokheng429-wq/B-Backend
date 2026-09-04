package com.bgroceries.backend.controller.Purchase;

import com.bgroceries.backend.dto.ReturnReceiptPODto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ReturnReceiptPOService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/return-receipt-pos")
@RequiredArgsConstructor
public class ReturnReceiptPOController {

    private final ReturnReceiptPOService returnReceiptPOService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnReceiptPODto>>> getAllReturnReceiptPOs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String searchBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) String status
    ) {
        List<ReturnReceiptPODto> list = returnReceiptPOService.getAllReturnReceiptPOs(search, searchBy, fromDate, toDate, outlet, status);
        return ResponseEntity.ok(ApiResponse.success("Return Receipt POs retrieved", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnReceiptPODto>> getReturnReceiptPOById(@PathVariable Long id) {
        ReturnReceiptPODto dto = returnReceiptPOService.getReturnReceiptPOById(id);
        return ResponseEntity.ok(ApiResponse.success("Return Receipt PO retrieved", dto));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String code = returnReceiptPOService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next Return Receipt PO code generated", Map.of("code", code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnReceiptPODto>> createReturnReceiptPO(@RequestBody ReturnReceiptPODto dto) {
        ReturnReceiptPODto created = returnReceiptPOService.createReturnReceiptPO(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return Receipt PO created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnReceiptPODto>> updateReturnReceiptPO(
            @PathVariable Long id,
            @RequestBody ReturnReceiptPODto dto
    ) {
        ReturnReceiptPODto updated = returnReceiptPOService.updateReturnReceiptPO(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Return Receipt PO updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReturnReceiptPODto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required"));
        }
        ReturnReceiptPODto updated = returnReceiptPOService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Return Receipt PO status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReturnReceiptPO(@PathVariable Long id) {
        returnReceiptPOService.deleteReturnReceiptPO(id);
        return ResponseEntity.ok(ApiResponse.success("Return Receipt PO deleted successfully", null));
    }
}
