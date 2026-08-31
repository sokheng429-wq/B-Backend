package com.bgroceries.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiveDocumentDto {

    private Long id;
    private String code;
    private LocalDate date;
    private String supplier;
    private String receiveType;
    private String reference;
    private String receivedBy;
    private String locationKey;
    private String template;
    private String noteType;
    private String note;
    private BigDecimal totalCost;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotEmpty
    @Valid
    private List<Line> lines;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Line {
        private Long id;

        @NotNull
        private Long productId;

        private String nameSnapshot;
        private BigDecimal qty;
        private BigDecimal unitCost;
        private String uom;
        private String serials;
        private BigDecimal qtyBefore;
        private BigDecimal qtyAfter;
        private BigDecimal lineTotal;
    }
}