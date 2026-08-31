package com.bgroceries.backend.dto;

import com.bgroceries.backend.entity.Stocks.StockDocument;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API shape for stock documents (Receive / Issue / Adjust). Field names mirror
 * the frontend ledger records so the client can post its documents verbatim:
 * code, date, supplier, receiveType, reference, receivedBy, lines[].
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDocumentDto {

    /** RECEIVE / ISSUE / ADJUST. */
    @NotNull
    private StockDocument.DocType docType;

    /** Server generates one when blank (GRN-000123 …). */
    public String code;

    public LocalDate date;

    // receive-specific
    public String supplier;
    public String receiveType;
    public String reference;
    public String receivedBy;
    public String locationKey;
    public String note;

    /** Denormalized total; recomputed from lines when null. */
    public BigDecimal totalCost;

    @NotEmpty
    @Valid
    private List<Line> lines;

    // ---- response-only fields ------------------------------------------------
    public Long id;
    public String status;
    public LocalDateTime createdAt;

    /** One product movement inside a document. */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Line {

        /** FK to the product whose stock moved — required. */
        @NotNull
        private Long productId;

        public String nameSnapshot;

        /** Receive/issue quantity; null for ADJUST lines. */
        public BigDecimal qty;

        /** ADJUST only: the physical count. */
        public BigDecimal countedQty;

        public BigDecimal unitCost;

        public String serials;

        // response-only: before/after on-hand as recorded when posted
        public BigDecimal qtyBefore;
        public BigDecimal qtyAfter;
        public BigDecimal lineTotal;
    }
}
