package com.bgroceries.backend.entity.Stocks;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adjustment_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdjustmentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "doc_date", nullable = false)
    private LocalDate docDate;

    @Column(name = "adjustment_type", length = 50)
    private String adjustmentType;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "adjusted_by", length = 100)
    private String adjustedBy;

    @Column(name = "outlet", length = 50)
    private String outlet;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "total_diff", precision = 12, scale = 3)
    private BigDecimal totalDiff;

    @Column(name = "total_cost", precision = 14, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdjustmentLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "Completed";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}