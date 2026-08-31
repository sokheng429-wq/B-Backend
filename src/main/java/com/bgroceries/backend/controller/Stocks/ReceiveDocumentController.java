package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.ReceiveDocumentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ReceiveDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/receive-documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReceiveDocumentController {

    private final ReceiveDocumentService receiveService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReceiveDocumentDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Receive documents retrieved", receiveService.getAll()));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<ReceiveDocumentDto>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Product receive history retrieved", receiveService.getByProductId(productId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReceiveDocumentDto>> create(@Valid @RequestBody ReceiveDocumentDto dto) {
        ReceiveDocumentDto created = receiveService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Receive document created successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        receiveService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Receive document deleted successfully"));
    }
}