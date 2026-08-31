package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    /** Newest unit first (createdAt is set by the entity's @PrePersist). */
    List<UnitOfMeasure> findAllByOrderByCreatedAtDesc();

    Optional<UnitOfMeasure> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseAndIdNot(String description, Long id);

    /**
     * Highest existing UN-#### sequence number, so the next generated code
     * never collides even after manual codes or deletions. Cast to a plain
     * long comparison — works identically on H2 and PostgreSQL.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(u.code, 4, LENGTH(u.code) - 3) AS long)), 0) " +
            "FROM UnitOfMeasure u WHERE u.code LIKE 'UN-%'")
    long maxSequenceNumber();
}
