package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.PaymentTermDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.PaymentTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment-terms")
@RequiredArgsConstructor
public class PaymentTermController {

    private final PaymentTermService paymentTermService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentTermDto>>> getAllPaymentTerms(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status
    ) {
        List<PaymentTermDto> list = paymentTermService.getAllPaymentTerms(search, searchBy, status);
        return ResponseEntity.ok(ApiResponse.success("Payment Terms retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentTermDto>> getPaymentTermById(@PathVariable Long id) {
        PaymentTermDto dto = paymentTermService.getPaymentTermById(id);
        return ResponseEntity.ok(ApiResponse.success("Payment Term retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentTermDto>> createPaymentTerm(@RequestBody PaymentTermDto dto) {
        PaymentTermDto created = paymentTermService.createPaymentTerm(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment Term created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentTermDto>> updatePaymentTerm(
            @PathVariable Long id,
            @RequestBody PaymentTermDto dto
    ) {
        PaymentTermDto updated = paymentTermService.updatePaymentTerm(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Payment Term updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PaymentTermDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Boolean active = null;
        if (body.containsKey("active")) {
            Object val = body.get("active");
            if (val instanceof Boolean) {
                active = (Boolean) val;
            } else if (val != null) {
                active = Boolean.parseBoolean(val.toString());
            }
        }
        PaymentTermDto updated = paymentTermService.updateStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePaymentTerm(@PathVariable Long id) {
        paymentTermService.deletePaymentTerm(id);
        return ResponseEntity.ok(ApiResponse.success("Payment Term deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<String>> getNextCode() {
        String nextCode = paymentTermService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next payment term code generated", nextCode));
    }
}
