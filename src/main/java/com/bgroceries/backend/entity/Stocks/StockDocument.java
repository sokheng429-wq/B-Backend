package com.bgroceries.backend.entity.Stocks;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A stock transaction document from the admin Stocks menu — Receive Products
 * (GRN), Issue Products (GI) and Adjustment (ADJ) all live in this one table,
 * distinguished by {@link DocType}. Each document owns its product lines
 * ({@link StockLine}), and every line points at the {@link Product} whose
 * on-hand quantity it moved, so the ledger is queryable per product.
 *
 * Transfer requests are NOT stored here — they only shuffle stock between
 * locations client-side today; a separate TransferRequest entity can be added
 * later without changing this table's shape.
 */
@Entity
@Table(name = "stock_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDocument {

    /** RECEIVE = goods in (raises on-hand), ISSUE = goods out, ADJUST = recount. */
    public enum DocType { RECEIVE, ISSUE, ADJUST }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human document number: GRN-000123 / GI-000456 / ADJ-000789 (unique). */
    @Column(name = "code", length = 30, nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", length = 10, nullable = false)
    private DocType docType;

    /** Document date chosen by the user (defaults to today). */
    @Column(name = "doc_date", nullable = false)
    private LocalDate docDate;

    // ---- receive-specific metadata (null for issue/adjust) -----------------
    @Column(name = "supplier", length = 150)
    private String supplier;

    @Column(name = "receive_type", length = 50)
    private String receiveType;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    /** Outlet/location key the movement applies to ('main', 'branch-a', …). */
    @Column(name = "location_key", length = 30)
    private String locationKey;

    /** Sum of line totals — kept denormalized so lists don't need a join. */
    @Column(name = "total_cost", precision = 14, scale = 2)
    private BigDecimal totalCost;

    /** Free-text note (stock receive note + detail). */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /** RECEIVED for receipts, POSTED for issue/adjust; reserved for workflows. */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /**
     * The user who posted the document. A plain string so the ledger survives
     * user deletion; swap for a User @ManyToOne when audit trails matter.
     */
    @Column(name = "posted_by", length = 100)
    private String postedBy;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "POSTED";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
