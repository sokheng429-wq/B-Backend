package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.WebOrderDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.WebOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/web-orders")
@RequiredArgsConstructor
public class WebOrderController {
    private final WebOrderService webOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WebOrderDto>>> getAllWebOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<WebOrderDto> list = webOrderService.getAllWebOrders(search, searchBy, status, outlet, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Web orders retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WebOrderDto>> getWebOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Web order retrieved successfully", webOrderService.getWebOrderById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WebOrderDto>> createWebOrder(@RequestBody WebOrderDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Web order created successfully", webOrderService.createWebOrder(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WebOrderDto>> updateWebOrder(@PathVariable Long id, @RequestBody WebOrderDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Web order updated successfully", webOrderService.updateWebOrder(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWebOrder(@PathVariable Long id) {
        webOrderService.deleteWebOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Web order deleted successfully"));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> getNextCode() {
        return ResponseEntity.ok(ApiResponse.success("Next code generated", Map.of("code", webOrderService.generateNextCode())));
    }
}
