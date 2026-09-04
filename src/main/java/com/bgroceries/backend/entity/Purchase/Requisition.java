package com.bgroceries.backend.entity.Purchase;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requisitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Requisition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "require_date")
    private LocalDate requireDate;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "requisition_type", length = 100)
    private String requisitionType;

    @Column(name = "requisition_amount")
    @Builder.Default
    private Double requisitionAmount = 0.0;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "OPEN"; // OPEN, PARTIAL, COMPLETED, VOIDED

    @Column(name = "note", length = 500)
    private String note;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequisitionItem> items = new ArrayList<>();

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
        if (this.requisitionAmount == null) {
            this.requisitionAmount = 0.0;
        }
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(RequisitionItem item) {
        items.add(item);
        item.setRequisition(this);
    }

    public void removeItem(RequisitionItem item) {
        items.remove(item);
        item.setRequisition(null);
    }
}
