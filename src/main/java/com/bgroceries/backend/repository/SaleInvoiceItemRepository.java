package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.SaleInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleInvoiceItemRepository extends JpaRepository<SaleInvoiceItem, Long> {
    List<SaleInvoiceItem> findBySaleInvoiceId(Long saleInvoiceId);
}