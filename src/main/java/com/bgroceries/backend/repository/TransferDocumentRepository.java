package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.TransferDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferDocumentRepository extends JpaRepository<TransferDocument, Long> {

    Optional<TransferDocument> findByCode(String code);

    List<TransferDocument> findAllByOrderByCreatedAtDesc();

    List<TransferDocument> findByDocTypeOrderByCreatedAtDesc(TransferDocument.DocType docType);

    List<TransferDocument> findByDocTypeAndStatusOrderByCreatedAtDesc(TransferDocument.DocType docType, String status);

    @Query("SELECT t FROM TransferDocument t WHERE (:docType IS NULL OR t.docType = :docType) AND (:status IS NULL OR t.status = :status) ORDER BY t.createdAt DESC")
    List<TransferDocument> filterTransfers(@Param("docType") TransferDocument.DocType docType, @Param("status") String status);
}