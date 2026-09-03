package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.ArCollectionDto;
import com.bgroceries.backend.dto.response.ApiResponse;
import com.bgroceries.backend.service.ArCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ar-collections")
@RequiredArgsConstructor
public class ArCollectionController {

    private final ArCollectionService collectionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArCollectionDto>>> getAllCollections(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<ArCollectionDto> list = collectionService.getAllCollections(
                search, searchBy, status, startDate, endDate
        );
        return ResponseEntity.ok(ApiResponse.success("AR Collections retrieved successfully", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArCollectionDto>> getCollectionById(@PathVariable Long id) {
        ArCollectionDto dto = collectionService.getCollectionById(id);
        return ResponseEntity.ok(ApiResponse.success("AR Collection retrieved successfully", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ArCollectionDto>> createCollection(@RequestBody ArCollectionDto dto) {
        ArCollectionDto created = collectionService.createCollection(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("AR Collection created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ArCollectionDto>> updateCollection(
            @PathVariable Long id,
            @RequestBody ArCollectionDto dto
    ) {
        ArCollectionDto updated = collectionService.updateCollection(id, dto);
        return ResponseEntity.ok(ApiResponse.success("AR Collection updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ArCollectionDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.getOrDefault("status", "NONE_VOID");
        ArCollectionDto updated = collectionService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.ok(ApiResponse.success("AR Collection deleted successfully", null));
    }

    @GetMapping("/next-code")
    public ResponseEntity<ApiResponse<String>> getNextCode() {
        String nextCode = collectionService.generateNextCode();
        return ResponseEntity.ok(ApiResponse.success("Next collection code generated", nextCode));
    }
}
