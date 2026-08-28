package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {

    /** All serial entries for a given product, newest first. */
    List<SerialNumber> findByProductIdOrderByCreatedAtDesc(Long productId);

    /** All entries for a given status across all products. */
    List<SerialNumber> findByStatus(SerialNumber.Status status);

    /** All entries created from a specific receive line. */
    List<SerialNumber> findByStockLineId(Long stockLineId);

    /** Look up by exact serial number string. */
    Optional<SerialNumber> findBySerialNumber(String serialNumber);
}
