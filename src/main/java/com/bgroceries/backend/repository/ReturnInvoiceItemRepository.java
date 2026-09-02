package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.ReturnInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnInvoiceItemRepository extends JpaRepository<ReturnInvoiceItem, Long> {
    List<ReturnInvoiceItem> findByReturnInvoiceId(Long returnInvoiceId);
}