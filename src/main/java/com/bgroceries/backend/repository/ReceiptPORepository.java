package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Purchase.ReceiptPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptPORepository extends JpaRepository<ReceiptPO, Long> {

    Optional<ReceiptPO> findByReceiptPoCode(String receiptPoCode);

    boolean existsByReceiptPoCode(String receiptPoCode);

    List<ReceiptPO> findAllByOrderByCreatedAtDesc();

    @Query("SELECT r FROM ReceiptPO r LEFT JOIN FETCH r.items WHERE r.id = :id")
    Optional<ReceiptPO> findByIdWithItems(Long id);

    @Query("SELECT r.receiptPoCode FROM ReceiptPO r WHERE r.receiptPoCode LIKE :prefix% ORDER BY r.receiptPoCode DESC")
    List<String> findCodesMatchingPrefix(String prefix);
}
