package com.bgroceries.backend.controller.Freight;

import com.bgroceries.backend.dto.ShipmentMethodDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ShipmentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shipment-methods")
@RequiredArgsConstructor
public class ShipmentMethodController {

    private final ShipmentMethodService shipmentMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentMethodDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status
    ) {
        List<ShipmentMethodDto> list = shipmentMethodService.getAll(search, searchBy, status);
        return ResponseEntity.ok(ApiResponse.success("Shipment methods retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentMethodDto>> getById(@PathVariable Long id) {
        ShipmentMethodDto dto = shipmentMethodService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Shipment method retrieved successfully", dto));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String code = shipmentMethodService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next code generated successfully", Map.of("code", code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentMethodDto>> create(@RequestBody ShipmentMethodDto request) {
        ShipmentMethodDto created = shipmentMethodService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment method created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentMethodDto>> update(
            @PathVariable Long id,
            @RequestBody ShipmentMethodDto request
    ) {
        ShipmentMethodDto updated = shipmentMethodService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Shipment method updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShipmentMethodDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Boolean active = null;
        if (body.containsKey("active")) {
            active = Boolean.valueOf(String.valueOf(body.get("active")));
        } else if (body.containsKey("status")) {
            String s = String.valueOf(body.get("status"));
            active = s.equalsIgnoreCase("active") || s.equalsIgnoreCase("true");
        }
        ShipmentMethodDto updated = shipmentMethodService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        shipmentMethodService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Shipment method deleted successfully", null));
    }
}
