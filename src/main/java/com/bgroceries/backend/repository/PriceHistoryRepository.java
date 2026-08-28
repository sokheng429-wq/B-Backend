package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    /** Full price history for a product, most recent first. */
    List<PriceHistory> findByProductIdOrderByChangedAtDesc(Long productId);

    /** All changes made by a specific user. */
    List<PriceHistory> findByChangedByOrderByChangedAtDesc(String changedBy);

    /** All changes of a specific change type (MANUAL / BULK_MARKUP / IMPORT). */
    List<PriceHistory> findByChangeTypeOrderByChangedAtDesc(PriceHistory.ChangeType changeType);
}
