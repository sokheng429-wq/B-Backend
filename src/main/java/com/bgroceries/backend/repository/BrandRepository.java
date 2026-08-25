package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    /** Newest brand first (createdAt is set by the entity's @PrePersist). */
    List<Brand> findAllByOrderByCreatedAtDesc();

    Optional<Brand> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseAndIdNot(String description, Long id);

    /**
     * Highest existing BR-#### sequence number, so the next generated code
     * never collides even after manual codes or deletions. Cast to a plain
     * long comparison — works identically on H2 and PostgreSQL.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(b.code, 4, LENGTH(b.code) - 3) AS long)), 0) " +
            "FROM Brand b WHERE b.code LIKE 'BR-%'")
    long maxSequenceNumber();
}
