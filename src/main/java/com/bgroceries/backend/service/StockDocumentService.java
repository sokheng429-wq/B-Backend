package com.bgroceries.backend.service;

import com.bgroceries.backend.dto.StockDocumentDto;
import com.bgroceries.backend.entity.Stocks.Product;
import com.bgroceries.backend.entity.Stocks.StockDocument;
import com.bgroceries.backend.entity.Stocks.StockLine;
import com.bgroceries.backend.exception.NotFoundException;
import com.bgroceries.backend.repository.ProductRepository;
import com.bgroceries.backend.repository.StockDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Stock ledger for the admin Stocks menu. Every Receive/Issue/Adjust document
 * is stored as a {@link StockDocument} whose lines each point at the
 * {@link Product} they moved — this is the database-level link between
 * "All Products" on-hand and the transaction history.
 *
 * Posting rules mirror the frontend logic:
 * - RECEIVE: on-hand += qty; moving-average cost recalculated from unitCost.
 * - ISSUE:   on-hand -= qty (floored at 0).
 * - ADJUST:  on-hand := countedQty (qtyBefore records the system count).
 *
 * Deleting a document reverses exactly what it posted, so ledgers and product
 * quantities can never drift apart — the same guarantee the frontend applies
 * when deleting rows from its lists.
 */
@Service
@RequiredArgsConstructor
public class StockDocumentService {

    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd", Locale.ROOT);

    private final StockDocumentRepository documentRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<StockDocumentDto> getAll(StockDocument.DocType docType) {
        List<StockDocument> docs = docType == null
                ? documentRepository.findAllByOrderByCreatedAtDesc()
                : documentRepository.findByDocTypeOrderByCreatedAtDesc(docType);
        return docs.stream().map(this::toDto).toList();
    }

    /** Every document that touched the given product — its stock history. */
    @Transactional(readOnly = true)
    public List<StockDocumentDto> getByProduct(Long productId) {
        return documentRepository.findDistinctByLines_Product_IdOrderByCreatedAtDesc(productId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Post a new document and apply its quantity deltas to the products.
     * Lines carry qtyBefore/qtyAfter snapshots so any later delete knows
     * exactly what to reverse without guessing.
     */
    @Transactional
    public StockDocumentDto create(StockDocumentDto dto) {
        StockDocument doc = StockDocument.builder()
                .code(resolveCode(dto))
                .docType(dto.getDocType())
                .docDate(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .supplier(normalize(dto.getSupplier()))
                .receiveType(normalize(dto.getReceiveType()))
                .reference(normalize(dto.getReference()))
                .receivedBy(normalize(dto.getReceivedBy()))
                .locationKey(normalize(dto.getLocationKey()))
                .note(normalize(dto.getNote()))
                .postedBy(normalize(dto.getReceivedBy())) // refine once auth context is wired in
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (StockDocumentDto.Line lineDto : dto.getLines()) {
            Product product = findProduct(lineDto.getProductId());
            BigDecimal before = orZero(product.getOnHand());

            StockLine line = StockLine.builder()
                    .product(product)
                    .nameSnapshot(lineDto.getNameSnapshot() != null ? lineDto.getNameSnapshot() : product.getName())
                    .unitCost(lineDto.getUnitCost())
                    .serials(normalize(lineDto.getSerials()))
                    .build();

            switch (doc.getDocType()) {
                case RECEIVE -> {
                    BigDecimal qty = orZero(lineDto.getQty());
                    if (qty.signum() <= 0) {
                        throw new IllegalArgumentException("Receive quantity must be positive");
                    }
                    line.setQty(qty);
                    line.setQtyBefore(before);
                    applyReceive(product, qty, lineDto.getUnitCost());
                    line.setQtyAfter(product.getOnHand());
                    total = total.add(orZero(line.getLineTotal()));
                }
                case ISSUE -> {
                    BigDecimal qty = orZero(lineDto.getQty());
                    if (qty.signum() <= 0) {
                        throw new IllegalArgumentException("Issue quantity must be positive");
                    }
                    line.setQty(qty);
                    line.setQtyBefore(before);
                    product.setOnHand(before.subtract(qty).max(BigDecimal.ZERO));
                    line.setQtyAfter(product.getOnHand());
                }
                case ADJUST -> {
                    BigDecimal counted = orZero(lineDto.getCountedQty() != null ? lineDto.getCountedQty() : lineDto.getQty());
                    line.setCountedQty(counted);
                    line.setQty(counted.subtract(before)); // signed diff for reporting
                    line.setQtyBefore(before);
                    product.setOnHand(counted);
                    line.setQtyAfter(counted);
                }
            }

            doc.getLines().add(line);
        }

        // dto.totalCost wins only if lines carried no costs to sum
        doc.setTotalCost(total.signum() != 0 ? total : dto.getTotalCost());
        return toDto(documentRepository.save(doc));
    }

    /**
     * Delete a document AND reverse its stock effect on every product it
     * touched — receive gives quantity back, issue returns issued units,
     * adjust restores the recorded before-count. This is what keeps All
     * Products on-hand consistent with the ledger.
     */
    @Transactional
    public void delete(Long id) {
        StockDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock document not found"));
        for (StockLine line : doc.getLines()) {
            Product product = line.getProduct();
            BigDecimal current = orZero(product.getOnHand());
            switch (doc.getDocType()) {
                case RECEIVE -> product.setOnHand(current.subtract(orZero(line.getQty())).max(BigDecimal.ZERO));
                case ISSUE -> product.setOnHand(current.add(orZero(line.getQty())));
                case ADJUST -> product.setOnHand(orZero(line.getQtyBefore()));
            }
            productRepository.save(product);
        }
        documentRepository.delete(doc); // cascades to lines
    }

    // ---- helpers -----------------------------------------------------------

    /** RECEIVE: on-hand += qty and recalculate the moving-average cost. */
    private void applyReceive(Product product, BigDecimal qty, BigDecimal unitCost) {
        BigDecimal before = orZero(product.getOnHand());
        BigDecimal cost = orZero(unitCost);
        if (cost.signum() == 0) {
            product.setOnHand(before.add(qty));
            return;
        }
        BigDecimal newOnHand = before.add(qty);
        BigDecimal oldValue = before.multiply(orZero(product.getAverageCost()));
        BigDecimal newValue = qty.multiply(cost);
        BigDecimal avg = newOnHand.signum() > 0
                ? oldValue.add(newValue).divide(newOnHand, 2, RoundingMode.HALF_UP)
                : cost;
        product.setOnHand(newOnHand);
        product.setAverageCost(avg);
    }

    /** GRN-260826-0007 style code, unique per day per type; user value kept when given. */
    private String resolveCode(StockDocumentDto dto) {
        String prefix = switch (dto.getDocType()) {
            case RECEIVE -> "GRN";
            case ISSUE -> "GI";
            case ADJUST -> "ADJ";
        };
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String requested = dto.getCode().trim();
            if (documentRepository.findByCode(requested).isPresent()) {
                throw new IllegalArgumentException("Document code already exists: " + requested);
            }
            return requested;
        }
        String datePart = LocalDate.now().format(CODE_DATE);
        int seq = 1;
        String candidate = prefix + "-" + datePart + "-" + seq;
        while (documentRepository.findByCode(candidate).isPresent()) {
            candidate = prefix + "-" + datePart + "-" + (++seq);
        }
        return candidate;
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    private BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private StockDocumentDto toDto(StockDocument doc) {
        List<StockDocumentDto.Line> lines = new ArrayList<>();
        for (StockLine l : doc.getLines()) {
            lines.add(StockDocumentDto.Line.builder()
                    .productId(l.getProduct().getId())
                    .nameSnapshot(l.getNameSnapshot())
                    .qty(l.getQty())
                    .countedQty(l.getCountedQty())
                    .qtyBefore(l.getQtyBefore())
                    .qtyAfter(l.getQtyAfter())
                    .unitCost(l.getUnitCost())
                    .lineTotal(l.getLineTotal())
                    .serials(l.getSerials())
                    .build());
        }
        return StockDocumentDto.builder()
                .id(doc.getId())
                .code(doc.getCode())
                .docType(doc.getDocType())
                .date(doc.getDocDate())
                .supplier(doc.getSupplier())
                .receiveType(doc.getReceiveType())
                .reference(doc.getReference())
                .receivedBy(doc.getReceivedBy())
                .locationKey(doc.getLocationKey())
                .note(doc.getNote())
                .totalCost(doc.getTotalCost())
                .status(doc.getStatus())
                .createdAt(doc.getCreatedAt())
                .lines(lines)
                .build();
    }
}
