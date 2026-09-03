package com.bgroceries.backend.entity.Sale;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnShipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_ship_code", length = 60, nullable = false, unique = true)
    private String returnShipCode;

    @Column(name = "so_code", length = 60)
    private String soCode;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "customer", length = 200, nullable = false)
    private String customer;

    @Column(name = "delivery_person", length = 100)
    private String deliveryPerson;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "RECEIVED"; // RECEIVED, RESTOCKED, REFUNDED, REJECTED

    @Column(name = "outlet", length = 100)
    private String outlet;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.date == null) this.date = LocalDateTime.now();
        if (this.status == null) this.status = "RECEIVED";
        if (this.amount == null) this.amount = BigDecimal.ZERO;
    }
}
