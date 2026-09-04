package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.AgingInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgingInvoiceRepository extends JpaRepository<AgingInvoice, Long> {
    Optional<AgingInvoice> findByCode(String code);
}
