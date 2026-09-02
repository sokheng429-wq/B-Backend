package com.bgroceries.backend.controller.Sale;

import com.bgroceries.backend.dto.SalePromotionDto;
import com.bgroceries.backend.service.SalePromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SalePromotionController {

    private final SalePromotionService promoService;

    @GetMapping
    public ResponseEntity<List<SalePromotionDto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "any") String searchBy,
            @RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(promoService.getAll(search, searchBy, activeOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalePromotionDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promoService.getById(id));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<SalePromotionDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(promoService.getByCode(code));
    }

    @PostMapping
    public ResponseEntity<SalePromotionDto> create(@RequestBody SalePromotionDto dto) {
        return new ResponseEntity<>(promoService.create(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalePromotionDto> update(@PathVariable Long id, @RequestBody SalePromotionDto dto) {
        return ResponseEntity.ok(promoService.update(id, dto));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<SalePromotionDto> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(promoService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        promoService.delete(id);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Promotion deleted successfully");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/next-code")
    public ResponseEntity<Map<String, String>> getNextCode() {
        Map<String, String> res = new HashMap<>();
        res.put("nextCode", promoService.getNextPromoCode());
        return ResponseEntity.ok(res);
    }
}