package com.bgroceries.backend.controller.Stocks;

import com.bgroceries.backend.dto.IssueDocumentDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.IssueDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/issue-documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IssueDocumentController {

    private final IssueDocumentService issueService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IssueDocumentDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Issue documents retrieved", issueService.getAll()));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<IssueDocumentDto>>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Product issue history retrieved", issueService.getByProductId(productId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IssueDocumentDto>> create(@Valid @RequestBody IssueDocumentDto dto) {
        IssueDocumentDto created = issueService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Issue document created successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        issueService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Issue document deleted successfully"));
    }
}