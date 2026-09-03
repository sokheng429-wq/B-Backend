package com.bgroceries.backend.entity.Sale;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "web_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code; // Order Code

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "salesperson", length = 100)
    private String salesperson;

    @Column(name = "customer_name", length = 200, nullable = false)
    private String customerName;

    @Column(name = "phone", length = 60)
    private String phone;

    @Column(name = "grand_total", precision = 18, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "balance", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSING, READY_TO_SHIP, DELIVERED, CANCELLED

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "markup", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal markup = BigDecimal.ZERO;

    @Column(name = "outlet", length = 100)
    private String outlet;

    @Column(name = "channel", length = 100)
    private String channel;

    @Column(name = "shipping_address", length = 300)
    private String shippingAddress;

    @OneToMany(mappedBy = "webOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<WebOrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.orderDate == null) this.orderDate = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
        if (this.grandTotal == null) this.grandTotal = BigDecimal.ZERO;
        if (this.balance == null) this.balance = this.grandTotal;
    }
}
