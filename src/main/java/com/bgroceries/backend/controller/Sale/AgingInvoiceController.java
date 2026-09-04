package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.AgingInvoiceDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.AgingInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/aging-invoices")
@RequiredArgsConstructor
public class AgingInvoiceController {

    private final AgingInvoiceService agingInvoiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AgingInvoiceDto>>> getAllAgingInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false, defaultValue = "ALL") String agingType,
            @RequestParam(required = false) String salesperson,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String customerGroup
    ) {
        List<AgingInvoiceDto> list = agingInvoiceService.getAllAgingInvoices(
                search, searchBy, agingType, salesperson, customer, customerGroup
        );
        return ResponseEntity.ok(ApiResponse.success("Aging invoices retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgingInvoiceDto>> getAgingInvoiceById(@PathVariable Long id) {
        AgingInvoiceDto dto = agingInvoiceService.getAgingInvoiceById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success("Aging invoice retrieved successfully", dto));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAgingSummary() {
        Map<String, Object> summary = agingInvoiceService.getAgingSummary();
        return ResponseEntity.ok(ApiResponse.success("Aging summary retrieved successfully", summary));
    }
}
