package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByOrderByCreatedAtDesc();

    Optional<Customer> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByCustomerNameIgnoreCase(String customerName);

    boolean existsByCustomerNameIgnoreCaseAndIdNot(String customerName, Long id);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(c.code, 4, LENGTH(c.code) - 3) AS long)), 0) FROM Customer c WHERE c.code LIKE 'CU-%'")
    long maxSequenceNumber();
}