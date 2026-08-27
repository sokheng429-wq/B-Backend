package com.bgroceries.backend.dto;

import com.bgroceries.backend.entity.TransferDocument;
import jakarta.validation.Valid;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDocumentDto {

    public Long id;

    public String code;

    @NotNull
    private TransferDocument.DocType docType;

    public LocalDate transferDate;
    public LocalDate requiredDate;
    public LocalDate requestTransferDate;

    public String fromOutlet;
    public String fromLocation;
    public String requestOutlet;
    public String requestLocation;
    public String toOutlet;
    public String toLocation;

    public String transferType;
    public String requestTransferType;
    public String reference;
    public String templateName;

    public String carrier;
    public String trackingNumber;
    public String dispatchNote;

    public String status;
    public String userName;
    public BigDecimal totalQty;

    @Valid
    private List<Line> lines;

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Line {
        public Long id;
        public Long productId;
        public String code;
        public String barCode;
        public String name;
        public String nameKh;
        public String uom;
        public BigDecimal onHand;
        public BigDecimal qty;
        public BigDecimal unitCost;
        public BigDecimal lineTotal;
    }
}