package com.bgroceries.backend.entity.Stocks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "issue_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @JsonIgnore
    private IssueDocument document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "name_snapshot", length = 200)
    private String nameSnapshot;

    @Column(name = "qty", precision = 12, scale = 3, nullable = false)
    private BigDecimal qty;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "uom", length = 30)
    private String uom;

    @Column(name = "qty_before", precision = 12, scale = 3)
    private BigDecimal qtyBefore;

    @Column(name = "qty_after", precision = 12, scale = 3)
    private BigDecimal qtyAfter;

    public BigDecimal getLineTotal() {
        if (qty == null || unitCost == null) return BigDecimal.ZERO;
        return qty.multiply(unitCost);
    }
}