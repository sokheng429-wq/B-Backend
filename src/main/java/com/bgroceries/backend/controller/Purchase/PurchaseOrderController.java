package com.bgroceries.backend.controller.Purchase;

import com.bgroceries.backend.dto.PurchaseOrderDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrderDto>>> getAllPurchaseOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) String purchasePerson,
            @RequestParam(required = false) String status
    ) {
        List<PurchaseOrderDto> list = purchaseOrderService.getAllPurchaseOrders(
                search, searchBy, fromDate, toDate, outlet, purchasePerson, status);
        return ResponseEntity.ok(ApiResponse.success("Purchase orders retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrderDto dto = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> createPurchaseOrder(@RequestBody PurchaseOrderDto dto) {
        PurchaseOrderDto created = purchaseOrderService.createPurchaseOrder(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Purchase order created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> updatePurchaseOrder(
            @PathVariable Long id,
            @RequestBody PurchaseOrderDto dto
    ) {
        PurchaseOrderDto updated = purchaseOrderService.updatePurchaseOrder(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Purchase order updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required"));
        }
        PurchaseOrderDto updated = purchaseOrderService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Purchase order status updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePurchaseOrder(@PathVariable Long id) {
        purchaseOrderService.deletePurchaseOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Purchase order deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        String code = purchaseOrderService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next purchase order code generated", Map.of("code", code)));
    }
}
