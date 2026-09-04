package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Purchase.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByCode(String code);

    boolean existsByCode(String code);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.items WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithItems(Long id);

    @Query("SELECT po.code FROM PurchaseOrder po WHERE po.code LIKE :prefix% ORDER BY po.code DESC")
    List<String> findCodesMatchingPrefix(String prefix);
}
