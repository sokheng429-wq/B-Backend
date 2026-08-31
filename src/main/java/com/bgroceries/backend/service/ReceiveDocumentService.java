package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.ReceiveDocumentDto;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.entity.Stocks.ReceiveDocument;
import com.bgroceries.backend.entity.Stocks.ReceiveLine;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductRepository;
import com.bgroceries.backend.repository.ReceiveDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReceiveDocumentService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final ReceiveDocumentRepository receiveRepo;
    private final ProductRepository productRepo;

    public List<ReceiveDocumentDto> getAll() {
        return receiveRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ReceiveDocumentDto> getByProductId(Long productId) {
        return receiveRepo.findByProductId(productId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReceiveDocumentDto create(ReceiveDocumentDto dto) {
        String code = resolveCode(dto.getCode());
        ReceiveDocument doc = ReceiveDocument.builder()
                .code(code)
                .docDate(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .supplier(normalize(dto.getSupplier()))
                .receiveType(normalize(dto.getReceiveType()))
                .reference(normalize(dto.getReference()))
                .receivedBy(normalize(dto.getReceivedBy()))
                .locationKey(normalize(dto.getLocationKey()))
                .template(normalize(dto.getTemplate()))
                .noteType(normalize(dto.getNoteType()))
                .note(normalize(dto.getNote()))
                .status("Received")
                .lines(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (ReceiveDocumentDto.Line lineDto : dto.getLines()) {
            Product product = productRepo.findById(lineDto.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + lineDto.getProductId()));
            BigDecimal before = orZero(product.getOnHand());
            BigDecimal qty = orZero(lineDto.getQty());
            BigDecimal unitCost = orZero(lineDto.getUnitCost());

            BigDecimal qtyAfter = before.add(qty);

            // update average cost and onHand
            if (unitCost.signum() > 0 && qtyAfter.signum() > 0) {
                BigDecimal oldValue = before.multiply(orZero(product.getAverageCost()));
                BigDecimal newValue = qty.multiply(unitCost);
                BigDecimal newAvg = oldValue.add(newValue).divide(qtyAfter, 2, RoundingMode.HALF_UP);
                product.setAverageCost(newAvg);
            }
            product.setOnHand(qtyAfter);
            productRepo.save(product);

            ReceiveLine line = ReceiveLine.builder()
                    .document(doc)
                    .product(product)
                    .nameSnapshot(lineDto.getNameSnapshot() != null ? lineDto.getNameSnapshot() : product.getName())
                    .qty(qty)
                    .unitCost(unitCost)
                    .uom(lineDto.getUom() != null ? lineDto.getUom() : product.getUom())
                    .serials(normalize(lineDto.getSerials()))
                    .qtyBefore(before)
                    .qtyAfter(qtyAfter)
                    .build();

            doc.getLines().add(line);
            total = total.add(line.getLineTotal());
        }

        doc.setTotalCost(total.signum() != 0 ? total : dto.getTotalCost());
        return toDto(receiveRepo.save(doc));
    }

    @Transactional
    public void delete(Long id) {
        ReceiveDocument doc = receiveRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Receive document not found"));
        for (ReceiveLine line : doc.getLines()) {
            Product product = line.getProduct();
            BigDecimal current = orZero(product.getOnHand());
            BigDecimal rolledBack = current.subtract(orZero(line.getQty())).max(BigDecimal.ZERO);
            product.setOnHand(rolledBack);
            productRepo.save(product);
        }
        receiveRepo.delete(doc);
    }

    private String resolveCode(String requested) {
        if (requested != null && !requested.isBlank()) {
            String trimmed = requested.trim();
            if (receiveRepo.findByCode(trimmed).isEmpty()) return trimmed;
        }
        String datePart = LocalDate.now().format(CODE_DATE);
        int seq = 1;
        String candidate = "GRN-" + datePart + "-" + seq;
        while (receiveRepo.findByCode(candidate).isPresent()) {
            candidate = "GRN-" + datePart + "-" + (++seq);
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

    private ReceiveDocumentDto toDto(ReceiveDocument d) {
        List<ReceiveDocumentDto.Line> lines = new ArrayList<>();
        for (ReceiveLine l : d.getLines()) {
            lines.add(ReceiveDocumentDto.Line.builder()
                    .id(l.getId())
                    .productId(l.getProduct().getId())
                    .nameSnapshot(l.getNameSnapshot())
                    .qty(l.getQty())
                    .unitCost(l.getUnitCost())
                    .uom(l.getUom())
                    .serials(l.getSerials())
                    .qtyBefore(l.getQtyBefore())
                    .qtyAfter(l.getQtyAfter())
                    .lineTotal(l.getLineTotal())
                    .build());
        }
        return ReceiveDocumentDto.builder()
                .id(d.getId())
                .code(d.getCode())
                .date(d.getDocDate())
                .supplier(d.getSupplier())
                .receiveType(d.getReceiveType())
                .reference(d.getReference())
                .receivedBy(d.getReceivedBy())
                .locationKey(d.getLocationKey())
                .template(d.getTemplate())
                .noteType(d.getNoteType())
                .note(d.getNote())
                .totalCost(d.getTotalCost())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .lines(lines)
                .build();
    }
}