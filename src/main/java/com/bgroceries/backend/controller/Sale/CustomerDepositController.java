package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.CustomerDepositDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.CustomerDepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/customer-deposits")
@RequiredArgsConstructor
public class CustomerDepositController {

    private final CustomerDepositService depositService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDepositDto>>> getAllDeposits(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<CustomerDepositDto> list = depositService.getAllDeposits(
                search, searchBy, status, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success("Customer deposits retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDepositDto>> getDepositById(@PathVariable Long id) {
        CustomerDepositDto dto = depositService.getDepositById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deposit retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDepositDto>> createDeposit(@RequestBody CustomerDepositDto dto) {
        CustomerDepositDto created = depositService.createDeposit(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer deposit created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDepositDto>> updateDeposit(
            @PathVariable Long id,
            @RequestBody CustomerDepositDto dto
    ) {
        CustomerDepositDto updated = depositService.updateDeposit(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Customer deposit updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomerDepositDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "NONE_VOID");
        CustomerDepositDto updated = depositService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDeposit(@PathVariable Long id) {
        depositService.deleteDeposit(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deposit deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<String>> getNextCode() {
        String nextCode = depositService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next deposit code generated", nextCode));
    }
}
