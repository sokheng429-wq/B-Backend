package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Cash.CashOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
    Optional<CashOperation> findByCode(String code);
    boolean existsByCode(String code);
    List<CashOperation> findAllByOrderByTransactionDateDesc();
}
