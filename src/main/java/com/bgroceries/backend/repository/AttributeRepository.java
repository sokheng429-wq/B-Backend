package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {

    /** Newest attribute first (createdAt is set by the entity's @PrePersist). */
    List<Attribute> findAllByOrderByCreatedAtDesc();

    Optional<Attribute> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByDescriptionIgnoreCase(String description);

    boolean existsByDescriptionIgnoreCaseAndIdNot(String description, Long id);

    /**
     * Highest existing AT-#### sequence number, so the next generated code
     * never collides even after manual codes or deletions.
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(a.code, 4, LENGTH(a.code) - 3) AS long)), 0) " +
            "FROM Attribute a WHERE a.code LIKE 'AT-%'")
    long maxSequenceNumber();
}
