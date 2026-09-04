package com.bgroceries.backend.entity.Freight;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String code;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "second_language", length = 255)
    private String secondLanguage;

    @Column(name = "cost_proration", length = 50, nullable = false)
    @Builder.Default
    private String costProration = "VALUE";

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "shipment_method_tariffs",
        joinColumns = @JoinColumn(name = "shipment_method_id"),
        inverseJoinColumns = @JoinColumn(name = "shipment_tariff_id")
    )
    @Builder.Default
    private List<ShipmentTariff> tariffs = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        if (this.costProration == null || this.costProration.isBlank()) {
            this.costProration = "VALUE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
