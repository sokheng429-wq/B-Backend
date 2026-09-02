package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.ReturnInvoiceDto;
import com.bgroceries.backend.service.ReturnInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/return-invoices")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReturnInvoiceController {

    private final ReturnInvoiceService returnInvoiceService;

    @GetMapping
    public ResponseEntity<List<ReturnInvoiceDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) String outlet,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(returnInvoiceService.getAll(search, searchBy, outlet, startDate, endDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnInvoiceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(returnInvoiceService.getById(id));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<ReturnInvoiceDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(returnInvoiceService.getByCode(code));
    }

    @PostMapping
    public ResponseEntity<ReturnInvoiceDto> create(@RequestBody ReturnInvoiceDto dto) {
        return new ResponseEntity<>(returnInvoiceService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReturnInvoiceDto> update(@PathVariable Long id, @RequestBody ReturnInvoiceDto dto) {
        return ResponseEntity.ok(returnInvoiceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        returnInvoiceService.delete(id);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Return invoice deleted successfully");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextCode() {
        Map<String, String> res = new HashMap<>();
        res.put("nextCode", returnInvoiceService.getNextReturnCode());
        return ResponseEntity.ok(res);
    }
}