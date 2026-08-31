package com.bgroceries.backend.entity.Stocks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One product line of a {@link StockDocument}. The {@code product} link is
 * the database-level join the frontend wants: every quantity that moved is
 * traceable back to the exact Product row, so "All Products" on-hand and the
 * Receive/Issue/Adjust ledgers can never drift apart silently.
 *
 * {@code nameSnapshot} keeps the product description as it was when the
 * document was posted, so old documents stay readable even after a rename.
 */
@Entity
@Table(name = "stock_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning document. Deleting a document cascades to its lines. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private StockDocument document;

    /** The product this movement applies to — the FK into the product table. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Description copied at posting time (survives product renames). */
    @Column(name = "name_snapshot", length = 200)
    private String nameSnapshot;

    /** Quantity received / issued. For ADJUST see countedQty instead. */
    @Column(name = "qty", precision = 12, scale = 3)
    private BigDecimal qty;

    /**
     * ADJUST only: what the physical count said. {@code qtyBefore} holds the
     * system count at posting time; diff = countedQty − qtyBefore.
     */
    @Column(name = "counted_qty", precision = 12, scale = 3)
    private BigDecimal countedQty;

    @Column(name = "qty_before", precision = 12, scale = 3)
    private BigDecimal qtyBefore;

    @Column(name = "qty_after", precision = 12, scale = 3)
    private BigDecimal qtyAfter;

    /** RECEIVE only: unit cost used for the moving-average recalculation. */
    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    /** Line total = qty × unit cost (receive) — denormalized for lists. */
    @Column(name = "line_total", precision = 14, scale = 2)
    private BigDecimal lineTotal;

    /** Serial numbers captured on the receive form, comma-separated. */
    @Column(name = "serials", length = 500)
    private String serials;

    @PrePersist
    protected void onCreate() {
        if (this.qtyAfter == null && this.qtyBefore != null && this.qty != null) {
            // default derivation; services may set qty_after explicitly
            this.qtyAfter = this.countedQty != null ? this.countedQty : this.qtyBefore.add(this.qty);
        }
        if (this.lineTotal == null && this.qty != null && this.unitCost != null) {
            this.lineTotal = this.qty.multiply(this.unitCost);
        }
    }
}
