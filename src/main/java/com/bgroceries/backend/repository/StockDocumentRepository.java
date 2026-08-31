package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.StockDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockDocumentRepository extends JpaRepository<StockDocument, Long> {

    Optional<StockDocument> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    /** Newest documents first — matches the frontend ledger ordering. */
    List<StockDocument> findAllByOrderByCreatedAtDesc();

    List<StockDocument> findByDocTypeOrderByCreatedAtDesc(StockDocument.DocType docType);

    /** Every document that touched a given product (via its lines). */
    List<StockDocument> findDistinctByLines_Product_IdOrderByCreatedAtDesc(Long productId);
}
