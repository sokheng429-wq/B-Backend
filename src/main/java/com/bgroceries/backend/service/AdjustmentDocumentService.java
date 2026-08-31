package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.AdjustmentDocumentDto;
import com.bgroceries.backend.entity.Stocks.AdjustmentDocument;
import com.bgroceries.backend.entity.Stocks.AdjustmentLine;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.AdjustmentDocumentRepository;
import com.bgroceries.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdjustmentDocumentService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final AdjustmentDocumentRepository adjRepo;
    private final ProductRepository productRepo;

    public List<AdjustmentDocumentDto> getAll() {
        return adjRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<AdjustmentDocumentDto> getByProductId(Long productId) {
        return adjRepo.findByProductId(productId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdjustmentDocumentDto create(AdjustmentDocumentDto dto) {
        String code = resolveCode(dto.getCode());
        AdjustmentDocument doc = AdjustmentDocument.builder()
                .code(code)
                .docDate(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .adjustmentType(normalize(dto.getAdjustmentType()))
                .reference(normalize(dto.getReference()))
                .adjustedBy(normalize(dto.getAdjustedBy()))
                .outlet(normalize(dto.getOutlet()))
                .note(normalize(dto.getNote()))
                .status("Completed")
                .lines(new ArrayList<>())
                .build();

        BigDecimal diffSum = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (AdjustmentDocumentDto.Line lineDto : dto.getLines()) {
            Product product = productRepo.findById(lineDto.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + lineDto.getProductId()));
            BigDecimal before = orZero(product.getOnHand());
            BigDecimal counted = orZero(lineDto.getCountedQty());
            BigDecimal diff = counted.subtract(before);
            BigDecimal unitCost = orZero(lineDto.getUnitCost());

            product.setOnHand(counted);
            productRepo.save(product);

            AdjustmentLine line = AdjustmentLine.builder()
                    .document(doc)
                    .product(product)
                    .nameSnapshot(lineDto.getNameSnapshot() != null ? lineDto.getNameSnapshot() : product.getName())
                    .countedQty(counted)
                    .qtyBefore(before)
                    .qtyDiff(diff)
                    .unitCost(unitCost)
                    .uom(lineDto.getUom() != null ? lineDto.getUom() : product.getUom())
                    .build();

            doc.getLines().add(line);
            diffSum = diffSum.add(diff);
            totalCost = totalCost.add(counted.multiply(unitCost));
        }

        doc.setTotalDiff(diffSum);
        doc.setTotalCost(totalCost.signum() != 0 ? totalCost : dto.getTotalCost());
        return toDto(adjRepo.save(doc));
    }

    @Transactional
    public void delete(Long id) {
        AdjustmentDocument doc = adjRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Adjustment document not found"));
        for (AdjustmentLine line : doc.getLines()) {
            Product product = line.getProduct();
            BigDecimal rolledBack = orZero(line.getQtyBefore());
            product.setOnHand(rolledBack);
            productRepo.save(product);
        }
        adjRepo.delete(doc);
    }

    private String resolveCode(String requested) {
        if (requested != null && !requested.isBlank()) {
            String trimmed = requested.trim();
            if (adjRepo.findByCode(trimmed).isEmpty()) return trimmed;
        }
        String datePart = LocalDate.now().format(CODE_DATE);
        int seq = 1;
        String candidate = "ADJ-" + datePart + "-" + seq;
        while (adjRepo.findByCode(candidate).isPresent()) {
            candidate = "ADJ-" + datePart + "-" + (++seq);
        }
        return candidate;
    }

    private BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private AdjustmentDocumentDto toDto(AdjustmentDocument d) {
        List<AdjustmentDocumentDto.Line> lines = new ArrayList<>();
        for (AdjustmentLine l : d.getLines()) {
            lines.add(AdjustmentDocumentDto.Line.builder()
                    .id(l.getId())
                    .productId(l.getProduct().getId())
                    .nameSnapshot(l.getNameSnapshot())
                    .countedQty(l.getCountedQty())
                    .qtyBefore(l.getQtyBefore())
                    .qtyDiff(l.getQtyDiff())
                    .unitCost(l.getUnitCost())
                    .uom(l.getUom())
                    .build());
        }
        return AdjustmentDocumentDto.builder()
                .id(d.getId())
                .code(d.getCode())
                .date(d.getDocDate())
                .adjustmentType(d.getAdjustmentType())
                .reference(d.getReference())
                .adjustedBy(d.getAdjustedBy())
                .outlet(d.getOutlet())
                .note(d.getNote())
                .totalDiff(d.getTotalDiff())
                .totalCost(d.getTotalCost())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .lines(lines)
                .build();
    }
}