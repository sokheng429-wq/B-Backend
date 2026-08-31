package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.ReceiveDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiveDocumentRepository extends JpaRepository<ReceiveDocument, Long> {
    List<ReceiveDocument> findAllByOrderByCreatedAtDesc();
    Optional<ReceiveDocument> findByCode(String code);

    @Query("SELECT d FROM ReceiveDocument d JOIN d.lines l WHERE l.product.id = :productId ORDER BY d.createdAt DESC")
    List<ReceiveDocument> findByProductId(@Param("productId") Long productId);
}