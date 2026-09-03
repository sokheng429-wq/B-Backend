package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.ShipmentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shipments")
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentDto>>> getAllShipments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<ShipmentDto> list = shipmentService.getAllShipments(search, searchBy, status, outlet, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Shipments retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentDto>> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Shipment retrieved successfully", shipmentService.getShipmentById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentDto>> createShipment(@RequestBody ShipmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Shipment created successfully", shipmentService.createShipment(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentDto>> updateShipment(@PathVariable Long id, @RequestBody ShipmentDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Shipment updated successfully", shipmentService.updateShipment(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShipment(@PathVariable Long id) {
        shipmentService.deleteShipment(id);
        return ResponseEntity.ok(ApiResponse.success("Shipment deleted successfully"));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        return ResponseEntity.ok(ApiResponse.success("Next code generated", Map.of("code", shipmentService.generateNextCode())));
    }
}
