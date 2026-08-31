package com.bgroceries.backend.entity.Stocks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "transfer_line")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_document_id", nullable = false)
    private TransferDocument transferDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "bar_code", length = 64)
    private String barCode;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "name_kh", length = 200)
    private String nameKh;

    @Column(name = "uom", length = 30)
    private String uom;

    @Column(name = "on_hand", precision = 12, scale = 3)
    private BigDecimal onHand;

    @Column(name = "qty", precision = 12, scale = 3, nullable = false)
    private BigDecimal qty;

    @Column(name = "unit_cost", precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "line_total", precision = 14, scale = 2)
    private BigDecimal lineTotal;
}