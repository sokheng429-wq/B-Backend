package com.bgroceries.backend.entity.Purchase;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "return_receipt_pos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnReceiptPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_po_code", length = 60, nullable = false, unique = true)
    private String returnPoCode;

    @Column(name = "po_code", length = 60)
    private String poCode;

    @Column(name = "receipt_po_code", length = 60)
    private String receiptPoCode;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "supplier", length = 150)
    private String supplier;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "amount")
    @Builder.Default
    private Double amount = 0.0;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "OPEN"; // OPEN, DISPATCHED, COMPLETED, VOIDED, PENDING

    @Column(name = "outlet", length = 150)
    private String outlet;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "note", length = 500)
    private String note;

    @OneToMany(mappedBy = "returnReceiptPO", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReturnReceiptPOItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null || this.status.isBlank()) {
            this.status = "OPEN";
        }
        if (this.amount == null) {
            this.amount = 0.0;
        }
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(ReturnReceiptPOItem item) {
        items.add(item);
        item.setReturnReceiptPO(this);
    }

    public void removeItem(ReturnReceiptPOItem item) {
        items.remove(item);
        item.setReturnReceiptPO(null);
    }
}
