package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.IssueDocumentDto;
import com.bgroceries.backend.entity.Stocks.IssueDocument;
import com.bgroceries.backend.entity.Stocks.IssueLine;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.IssueDocumentRepository;
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
public class IssueDocumentService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final IssueDocumentRepository issueRepo;
    private final ProductRepository productRepo;

    public List<IssueDocumentDto> getAll() {
        return issueRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<IssueDocumentDto> getByProductId(Long productId) {
        return issueRepo.findByProductId(productId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public IssueDocumentDto create(IssueDocumentDto dto) {
        String code = resolveCode(dto.getCode());
        IssueDocument doc = IssueDocument.builder()
                .code(code)
                .docDate(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .issueType(normalize(dto.getIssueType()))
                .reference(normalize(dto.getReference()))
                .issuedBy(normalize(dto.getIssuedBy()))
                .outlet(normalize(dto.getOutlet()))
                .note(normalize(dto.getNote()))
                .status("Completed")
                .lines(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (IssueDocumentDto.Line lineDto : dto.getLines()) {
            Product product = productRepo.findById(lineDto.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + lineDto.getProductId()));
            BigDecimal before = orZero(product.getOnHand());
            BigDecimal qty = orZero(lineDto.getQty());
            BigDecimal unitCost = orZero(lineDto.getUnitCost());

            BigDecimal qtyAfter = before.subtract(qty).max(BigDecimal.ZERO);
            product.setOnHand(qtyAfter);
            productRepo.save(product);

            IssueLine line = IssueLine.builder()
                    .document(doc)
                    .product(product)
                    .nameSnapshot(lineDto.getNameSnapshot() != null ? lineDto.getNameSnapshot() : product.getName())
                    .qty(qty)
                    .unitCost(unitCost)
                    .uom(lineDto.getUom() != null ? lineDto.getUom() : product.getUom())
                    .qtyBefore(before)
                    .qtyAfter(qtyAfter)
                    .build();

            doc.getLines().add(line);
            total = total.add(line.getLineTotal());
        }

        doc.setTotalCost(total.signum() != 0 ? total : dto.getTotalCost());
        return toDto(issueRepo.save(doc));
    }

    @Transactional
    public void delete(Long id) {
        IssueDocument doc = issueRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Issue document not found"));
        for (IssueLine line : doc.getLines()) {
            Product product = line.getProduct();
            BigDecimal current = orZero(product.getOnHand());
            BigDecimal rolledBack = current.add(orZero(line.getQty()));
            product.setOnHand(rolledBack);
            productRepo.save(product);
        }
        issueRepo.delete(doc);
    }

    private String resolveCode(String requested) {
        if (requested != null && !requested.isBlank()) {
            String trimmed = requested.trim();
            if (issueRepo.findByCode(trimmed).isEmpty()) return trimmed;
        }
        String datePart = LocalDate.now().format(CODE_DATE);
        int seq = 1;
        String candidate = "GI-" + datePart + "-" + seq;
        while (issueRepo.findByCode(candidate).isPresent()) {
            candidate = "GI-" + datePart + "-" + (++seq);
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

    private IssueDocumentDto toDto(IssueDocument d) {
        List<IssueDocumentDto.Line> lines = new ArrayList<>();
        for (IssueLine l : d.getLines()) {
            lines.add(IssueDocumentDto.Line.builder()
                    .id(l.getId())
                    .productId(l.getProduct().getId())
                    .nameSnapshot(l.getNameSnapshot())
                    .qty(l.getQty())
                    .unitCost(l.getUnitCost())
                    .uom(l.getUom())
                    .qtyBefore(l.getQtyBefore())
                    .qtyAfter(l.getQtyAfter())
                    .lineTotal(l.getLineTotal())
                    .build());
        }
        return IssueDocumentDto.builder()
                .id(d.getId())
                .code(d.getCode())
                .date(d.getDocDate())
                .issueType(d.getIssueType())
                .reference(d.getReference())
                .issuedBy(d.getIssuedBy())
                .outlet(d.getOutlet())
                .note(d.getNote())
                .totalCost(d.getTotalCost())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .lines(lines)
                .build();
    }
}