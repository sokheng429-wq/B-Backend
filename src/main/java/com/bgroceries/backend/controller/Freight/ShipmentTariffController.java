package com.bgroceries.backend.controller.Freight;

import com.bgroceries.backend.dto.ShipmentTariffDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ShipmentTariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shipment-tariffs")
@RequiredArgsConstructor
public class ShipmentTariffController {

    private final ShipmentTariffService shipmentTariffService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentTariffDto>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status
    ) {
        List<ShipmentTariffDto> list = shipmentTariffService.getAll(search, searchBy, status);
        return ResponseEntity.ok(ApiResponse.success("Shipment tariffs retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentTariffDto>> getById(@PathVariable Long id) {
        ShipmentTariffDto dto = shipmentTariffService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Shipment tariff retrieved successfully", dto));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String code = shipmentTariffService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next code generated successfully", Map.of("code", code)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentTariffDto>> create(@RequestBody ShipmentTariffDto request) {
        ShipmentTariffDto created = shipmentTariffService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Shipment tariff created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShipmentTariffDto>> update(
            @PathVariable Long id,
            @RequestBody ShipmentTariffDto request
    ) {
        ShipmentTariffDto updated = shipmentTariffService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Shipment tariff updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ShipmentTariffDto>> updateStatus(
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
        ShipmentTariffDto updated = shipmentTariffService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        shipmentTariffService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Shipment tariff deleted successfully", null));
    }
}
