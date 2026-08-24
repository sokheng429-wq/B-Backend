package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Newest product first (createdAt is set by the entity's @PrePersist). */
    List<Product> findAllByOrderByCreatedAtDesc();

    Optional<Product> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
