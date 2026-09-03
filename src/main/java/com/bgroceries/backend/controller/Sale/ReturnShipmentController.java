package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.ReturnShipmentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ReturnShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/return-shipments")
@RequiredArgsConstructor
public class ReturnShipmentController {
    private final ReturnShipmentService returnShipmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnShipmentDto>>> getAllReturnShipments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<ReturnShipmentDto> list = returnShipmentService.getAllReturnShipments(search, searchBy, status, outlet, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Return shipments retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnShipmentDto>> getReturnShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Return shipment retrieved successfully", returnShipmentService.getReturnShipmentById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnShipmentDto>> createReturnShipment(@RequestBody ReturnShipmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Return shipment created successfully", returnShipmentService.createReturnShipment(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnShipmentDto>> updateReturnShipment(@PathVariable Long id, @RequestBody ReturnShipmentDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Return shipment updated successfully", returnShipmentService.updateReturnShipment(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReturnShipment(@PathVariable Long id) {
        returnShipmentService.deleteReturnShipment(id);
        return ResponseEntity.ok(ApiResponse.success("Return shipment deleted successfully"));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        return ResponseEntity.ok(ApiResponse.success("Next code generated", Map.of("code", returnShipmentService.generateNextCode())));
    }
}
