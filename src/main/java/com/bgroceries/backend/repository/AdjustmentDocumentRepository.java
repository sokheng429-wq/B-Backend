package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.AdjustmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdjustmentDocumentRepository extends JpaRepository<AdjustmentDocument, Long> {
    List<AdjustmentDocument> findAllByOrderByCreatedAtDesc();
    Optional<AdjustmentDocument> findByCode(String code);

    @Query("SELECT d FROM AdjustmentDocument d JOIN d.lines l WHERE l.product.id = :productId ORDER BY d.createdAt DESC")
    List<AdjustmentDocument> findByProductId(@Param("productId") Long productId);
}