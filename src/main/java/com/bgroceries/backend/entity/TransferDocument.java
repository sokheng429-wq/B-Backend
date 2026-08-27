package com.bgroceries.backend.entity;

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

@Entity
@Table(name = "transfer_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferDocument {

    public enum DocType { REQUEST, TRANSFER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", length = 20, nullable = false)
    private DocType docType;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "request_transfer_date")
    private LocalDate requestTransferDate;

    @Column(name = "from_outlet", length = 100)
    private String fromOutlet;

    @Column(name = "from_location", length = 100)
    private String fromLocation;

    @Column(name = "to_outlet", length = 100)
    private String toOutlet;

    @Column(name = "to_location", length = 100)
    private String toLocation;

    @Column(name = "transfer_type", length = 100)
    private String transferType;

    @Column(name = "reference", length = 150)
    private String reference;

    @Column(name = "template_name", length = 100)
    private String templateName;

    @Column(name = "carrier", length = 150)
    private String carrier;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "dispatch_note", columnDefinition = "TEXT")
    private String dispatchNote;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "total_qty", precision = 12, scale = 3)
    private BigDecimal totalQty;

    @OneToMany(mappedBy = "transferDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransferLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.transferDate == null) this.transferDate = LocalDate.now();
        if (this.status == null) this.status = (this.docType == DocType.REQUEST ? "PENDING" : "COMPLETED");
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}