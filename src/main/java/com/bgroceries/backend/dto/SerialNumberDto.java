package com.bgroceries.backend.dto;

import com.bgroceries.backend.entity.SerialNumber;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * API shape for {@code SerialNumber} — flat DTO with no nested objects.
 * {@code productId} and {@code stockLineId} are the FK references.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialNumberDto {

    // response-only
    public Long id;
    public LocalDateTime createdAt;

    // FK references
    public Long productId;
    public Long stockLineId;

    // serial / batch data
    public String serialNumber;
    public String batchLot;
    public LocalDate expiryDate;
    public SerialNumber.Status status;

    // snapshots
    public String productCode;
    public String productName;
}
