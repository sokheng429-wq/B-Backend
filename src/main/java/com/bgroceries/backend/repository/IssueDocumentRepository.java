package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.IssueDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueDocumentRepository extends JpaRepository<IssueDocument, Long> {
    List<IssueDocument> findAllByOrderByCreatedAtDesc();
    Optional<IssueDocument> findByCode(String code);

    @Query("SELECT d FROM IssueDocument d JOIN d.lines l WHERE l.product.id = :productId ORDER BY d.createdAt DESC")
    List<IssueDocument> findByProductId(@Param("productId") Long productId);
}