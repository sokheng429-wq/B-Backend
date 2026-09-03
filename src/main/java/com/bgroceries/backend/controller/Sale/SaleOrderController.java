package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.SaleOrderDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.SaleOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sale-orders")
@RequiredArgsConstructor
public class SaleOrderController {
    private final SaleOrderService saleOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleOrderDto>>> getAllSaleOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<SaleOrderDto> list = saleOrderService.getAllSaleOrders(search, searchBy, status, outlet, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Sale orders retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleOrderDto>> getSaleOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Sale order retrieved successfully", saleOrderService.getSaleOrderById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SaleOrderDto>> createSaleOrder(@RequestBody SaleOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Sale order created successfully", saleOrderService.createSaleOrder(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SaleOrderDto>> updateSaleOrder(@PathVariable Long id, @RequestBody SaleOrderDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Sale order updated successfully", saleOrderService.updateSaleOrder(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSaleOrder(@PathVariable Long id) {
        saleOrderService.deleteSaleOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Sale order deleted successfully"));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        return ResponseEntity.ok(ApiResponse.success("Next code generated", Map.of("code", saleOrderService.generateNextCode())));
    }
}
