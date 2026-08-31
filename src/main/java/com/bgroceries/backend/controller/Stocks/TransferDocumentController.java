package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.TransferDocumentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.entity.Stocks.TransferDocument;
import com.bgroceries.backend.service.TransferDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/transfers")
@RequiredArgsConstructor
public class TransferDocumentController {

    private final TransferDocumentService transferDocumentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransferDocumentDto>>> getAll(
            @RequestParam(required = false) TransferDocument.DocType docType,
            @RequestParam(required = false) String status) {
        List<TransferDocumentDto> docs = transferDocumentService.getAll(docType, status);
        return ResponseEntity.ok(ApiResponse.success("Transfer documents retrieved successfully", docs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransferDocumentDto>> getById(@PathVariable Long id) {
        TransferDocumentDto doc = transferDocumentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Transfer document retrieved successfully", doc));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransferDocumentDto>> create(@Valid @RequestBody TransferDocumentDto dto) {
        TransferDocumentDto created = transferDocumentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer document created successfully", created));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TransferDocumentDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody TransferDocumentDto dto) {
        TransferDocumentDto updated = transferDocumentService.updateStatus(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Transfer document status updated successfully", updated));
    }

    @PostMapping("/ship-bulk")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkShip(@RequestBody(required = false) Map<String, Object> payload) {
        List<Long> ids = null;
        String carrier = "B-Express Cold Van #04";
        String dispatchNote = "Bulk dispatched from fulfillment hub";

        if (payload != null) {
            if (payload.get("ids") instanceof List<?> list) {
                ids = list.stream().map(o -> Long.valueOf(o.toString())).toList();
            }
            if (payload.get("carrier") instanceof String c) carrier = c;
            if (payload.get("dispatchNote") instanceof String n) dispatchNote = n;
        }

        int count = transferDocumentService.bulkShip(ids, carrier, dispatchNote);
        return ResponseEntity.ok(ApiResponse.success("Bulk dispatched transfers", Map.of("count", count)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        transferDocumentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Transfer document deleted successfully"));
    }
}