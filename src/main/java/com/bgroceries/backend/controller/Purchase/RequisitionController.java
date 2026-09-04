package com.bgroceries.backend.controller.Purchase;

import com.bgroceries.backend.dto.RequisitionDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.RequisitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/requisitions")
@RequiredArgsConstructor
public class RequisitionController {

    private final RequisitionService requisitionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RequisitionDto>>> getAllRequisitions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status
    ) {
        List<RequisitionDto> list = requisitionService.getAllRequisitions(search, searchBy, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.success("Requisitions retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequisitionDto>> getRequisitionById(@PathVariable Long id) {
        RequisitionDto dto = requisitionService.getRequisitionById(id);
        return ResponseEntity.ok(ApiResponse.success("Requisition retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RequisitionDto>> createRequisition(@RequestBody RequisitionDto dto) {
        RequisitionDto created = requisitionService.createRequisition(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Requisition created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RequisitionDto>> updateRequisition(
            @PathVariable Long id,
            @RequestBody RequisitionDto dto
    ) {
        RequisitionDto updated = requisitionService.updateRequisition(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Requisition updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RequisitionDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "OPEN");
        RequisitionDto updated = requisitionService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Requisition status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRequisition(@PathVariable Long id) {
        requisitionService.deleteRequisition(id);
        return ResponseEntity.ok(ApiResponse.success("Requisition deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String nextCode = requisitionService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next requisition code generated", Map.of("code", nextCode)));
    }
}
