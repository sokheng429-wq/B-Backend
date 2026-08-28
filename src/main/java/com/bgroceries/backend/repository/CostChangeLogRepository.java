package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.CostChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostChangeLogRepository extends JpaRepository<CostChangeLog, Long> {

    /** Full cost history for a product, most recent first. */
    List<CostChangeLog> findByProductIdOrderByChangedAtDesc(Long productId);

    /** All changes made by a specific user. */
    List<CostChangeLog> findByChangedByOrderByChangedAtDesc(String changedBy);

    /** All changes of a specific adjustment type. */
    List<CostChangeLog> findByAdjustmentTypeOrderByChangedAtDesc(CostChangeLog.AdjustmentType adjustmentType);
}
