package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.PaymentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {

    Optional<PaymentTerm> findByCode(String code);

    List<PaymentTerm> findAllByOrderByDaysAsc();

    List<PaymentTerm> findAllByOrderByCreatedAtDesc();
}
