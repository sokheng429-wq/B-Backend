package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ship_code", length = 60, nullable = false, unique = true)
    private String shipCode;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "customer", length = 200, nullable = false)
    private String customer;

    @Column(name = "phone", length = 60)
    private String phone;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "delivery_person", length = 100)
    private String deliveryPerson;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "READY"; // READY, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED

    @Column(name = "salesperson", length = 100)
    private String salesperson;

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "outlet", length = 100)
    private String outlet;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "destination", length = 300)
    private String destination;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.date == null) this.date = LocalDateTime.now();
        if (this.status == null) this.status = "READY";
        if (this.amount == null) this.amount = BigDecimal.ZERO;
        if (this.balance == null) this.balance = BigDecimal.ZERO;
    }
}
