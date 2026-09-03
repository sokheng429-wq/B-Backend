package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.ConsignmentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ConsignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/consignments")
@RequiredArgsConstructor
public class ConsignmentController {

    private final ConsignmentService consignmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsignmentDto>>> getAllConsignments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String salesperson,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<ConsignmentDto> list = consignmentService.getAllConsignments(
                search, searchBy, status, outlet, customer, salesperson, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success("Consignments retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsignmentDto>> getConsignmentById(@PathVariable Long id) {
        ConsignmentDto dto = consignmentService.getConsignmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Consignment retrieved successfully", dto));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<ApiResponse<ConsignmentDto>> getConsignmentByCode(@PathVariable String code) {
        ConsignmentDto dto = consignmentService.getConsignmentByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Consignment retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConsignmentDto>> createConsignment(@RequestBody ConsignmentDto dto) {
        ConsignmentDto created = consignmentService.createConsignment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consignment created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConsignmentDto>> updateConsignment(
            @PathVariable Long id,
            @RequestBody ConsignmentDto dto
    ) {
        ConsignmentDto updated = consignmentService.updateConsignment(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Consignment updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ConsignmentDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "OPEN");
        ConsignmentDto updated = consignmentService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConsignment(@PathVariable Long id) {
        consignmentService.deleteConsignment(id);
        return ResponseEntity.ok(ApiResponse.success("Consignment deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<String>> getNextCode() {
        String nextCode = consignmentService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next consignment code generated", nextCode));
    }
}
