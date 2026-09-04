package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Purchase.Requisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequisitionRepository extends JpaRepository<Requisition, Long> {

    Optional<Requisition> findByCode(String code);

    List<Requisition> findAllByOrderByCreatedAtDesc();
}
