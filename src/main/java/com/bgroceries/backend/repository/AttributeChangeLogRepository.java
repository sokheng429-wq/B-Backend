package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.AttributeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeChangeLogRepository extends JpaRepository<AttributeChangeLog, Long> {

    /** Full attribute change history for a product, most recent first. */
    List<AttributeChangeLog> findByProductIdOrderByChangedAtDesc(Long productId);

    /** All changes to a named attribute across all products. */
    List<AttributeChangeLog> findByAttributeNameOrderByChangedAtDesc(String attributeName);

    /** All changes made by a specific user. */
    List<AttributeChangeLog> findByChangedByOrderByChangedAtDesc(String changedBy);
}
