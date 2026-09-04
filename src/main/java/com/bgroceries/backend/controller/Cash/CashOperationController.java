package com.bgroceries.backend.controller.Cash;

import com.bgroceries.backend.dto.CashOperationDto;
import com.bgroceries.backend.service.CashOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/cash-operations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CashOperationController {

    private final CashOperationService service;

    @GetMapping
    public ResponseEntity<List<CashOperationDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "Any") String searchBy,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        List<CashOperationDto> list = service.searchOperations(search, searchBy, type, outlet, status, fromDate, toDate);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashOperationDto> getById(@PathVariable Long id) {
        CashOperationDto dto = service.getById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/void")
    public ResponseEntity<CashOperationDto> voidOperation(@PathVariable Long id) {
        CashOperationDto dto = service.voidOperation(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }
}
