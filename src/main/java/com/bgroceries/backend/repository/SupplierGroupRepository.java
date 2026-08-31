package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.SupplierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplierGroupRepository extends JpaRepository<SupplierGroup, Long> {

    /** Newest group first (createdAt is set by the entity's @PrePersist). */
    List<SupplierGroup> findAllByOrderByCreatedAtDesc();

    Optional<SupplierGroup> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseAndIdNot(String description, Long id);

    /**
     * Highest existing SG-#### sequence number, so the next generated code
     * never collides even after manual codes or deletions. Cast to a plain
     * long comparison — works identically on H2 and PostgreSQL.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(g.code, 4, LENGTH(g.code) - 3) AS long)), 0) " +
            "FROM SupplierGroup g WHERE g.code LIKE 'SG-%'")
    long maxSequenceNumber();
}
