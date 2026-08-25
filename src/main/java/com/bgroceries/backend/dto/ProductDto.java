package com.bgroceries.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Combined request/response DTO for the Product resource (admin Stocks →
 * Products CRUD). Field names are part of the API contract — do not rename.
 * Only {@code name} is required; everything else is optional master data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long id;

    @Size(max = 50, message = "Code must be at most 50 characters")
    private String code;

    @Size(max = 64, message = "Barcode must be at most 64 characters")
    private String barCode;

    @NotBlank(message = "Product name is required")
    private String name;

    /** Second-language (Khmer) display name. */
    private String nameKh;

    private String description;

    private String productGroup;

    private String category;

    /** Units physically on hand. */
    private BigDecimal onHand;

    private String uom;

    @DecimalMin(value = "0", message = "Base price must be zero or more")
    private BigDecimal basePrice;

    /** Average (available) cost. */
    private BigDecimal averageCost;

    private BigDecimal standardCost;

    /** Business-facing creation date; defaults to today when omitted on create. */
    private LocalDate createDate;

    private String country;

    private String supplier;

    private String partNumber;

    private String brand;

    /** Quantity incoming on purchase orders. */
    private BigDecimal onPo;

    /** Quantity committed on sales orders. */
    private BigDecimal onSo;

    private BigDecimal availableStock;

    private Boolean active;

    private String serial;

    private LocalDate expiryDate;

    private Boolean allowDiscount;

    /** Tax rate percent. */
    private BigDecimal tax;

    private Boolean outOfStock;

    private Boolean favorite;

    /** URL or compressed base64 data URL (see Product.imageUrl). */
    private String imageUrl;

    /** Read-only audit stamps — populated from the entity. */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
