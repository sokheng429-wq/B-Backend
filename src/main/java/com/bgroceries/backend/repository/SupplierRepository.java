package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /** Newest supplier first (createdAt is set by the entity's @PrePersist). */
    List<Supplier> findAllByOrderByCreatedAtDesc();

    Optional<Supplier> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Highest existing SP-#### sequence number, so the next generated code
     * never collides even after manual codes or deletions. Cast to a plain
     * long comparison — works identically on H2 and PostgreSQL.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.code, 4, LENGTH(s.code) - 3) AS long)), 0) " +
            "FROM Supplier s WHERE s.code LIKE 'SP-%'")
    long maxSequenceNumber();
}
