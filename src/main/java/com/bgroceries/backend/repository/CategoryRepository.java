package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Newest category first (createdAt is set by the entity's @PrePersist). */
    List<Category> findAllByOrderByCreatedAtDesc();

    Optional<Category> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseAndIdNot(String description, Long id);

    /**
     * Highest existing CT-#### sequence number, so the next generated code
     * never collides even after manual codes or deletions. Cast to a plain
     * long comparison — works identically on H2 and PostgreSQL.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.code, 4, LENGTH(c.code) - 3) AS long)), 0) " +
            "FROM Category c WHERE c.code LIKE 'CT-%'")
    long maxSequenceNumber();
}
