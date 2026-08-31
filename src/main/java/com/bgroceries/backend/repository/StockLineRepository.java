package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.StockLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockLineRepository extends JpaRepository<StockLine, Long> {

    /** Full movement history of one product, newest first. */
    List<StockLine> findByProduct_IdOrderByDocument_CreatedAtDesc(Long productId);

    /** Lines of a single document, in insertion order. */
    List<StockLine> findByDocument_IdOrderByIdAsc(Long documentId);
}
