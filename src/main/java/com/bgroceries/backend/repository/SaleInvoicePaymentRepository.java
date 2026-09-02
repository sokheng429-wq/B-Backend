package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.SaleInvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleInvoicePaymentRepository extends JpaRepository<SaleInvoicePayment, Long> {
    List<SaleInvoicePayment> findBySaleInvoiceIdOrderByPaymentDateDesc(Long saleInvoiceId);
}