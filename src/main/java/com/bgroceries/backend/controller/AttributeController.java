package com.bgroceries.backend.controller;

import com.bgroceries.backend.dto.AttributeDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.AttributeService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin attribute management endpoints (Stocks → Attributes), all wrapped in
 * the standard {@link ApiResponse} envelope. ROLE_ADMIN is enforced by the
 * existing SecurityConfig {@code /api/admin/**} matcher — no
 * {@code @PreAuthorize} needed.
 */
@RestController
@RequestMapping("/api/admin/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final AttributeService attributeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttributeDto>>> getAllAttributes() {
        List<AttributeDto> attributes = attributeService.getAllAttributes();
        return ResponseEntity.ok(ApiResponse.success("Attributes retrieved successfully", attributes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttributeDto>> getAttributeById(@PathVariable Long id) {
        AttributeDto attribute = attributeService.getAttributeById(id);
        return ResponseEntity.ok(ApiResponse.success("Attribute retrieved successfully", attribute));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttributeDto>> createAttribute(@Valid @RequestBody AttributeDto dto) {
        AttributeDto created = attributeService.createAttribute(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attribute created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttributeDto>> updateAttribute(@PathVariable Long id, @Valid @RequestBody AttributeDto dto) {
        AttributeDto updated = attributeService.updateAttribute(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Attribute updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttribute(@PathVariable Long id) {
        attributeService.deleteAttribute(id);
        return ResponseEntity.ok(ApiResponse.success("Attribute deleted successfully"));
    }
}
