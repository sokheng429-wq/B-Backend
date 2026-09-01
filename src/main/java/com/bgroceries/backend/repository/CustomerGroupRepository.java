package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {

    Optional<CustomerGroup> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT cg FROM CustomerGroup cg WHERE LOWER(cg.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(cg.code) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(cg.secondLanguage) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY cg.id DESC")
    List<CustomerGroup> search(String query);

    List<CustomerGroup> findAllByOrderByCreatedAtDesc();
}