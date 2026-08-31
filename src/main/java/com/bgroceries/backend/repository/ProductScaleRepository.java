package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Stocks.ProductScale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductScaleRepository extends JpaRepository<ProductScale, Long> {

    /** All scale configurations for a given product. */
    List<ProductScale> findByProductId(Long productId);

    /** All active scale entries. */
    List<ProductScale> findByActiveTrue();

    /** Lookup by PLU code (unique). */
    Optional<ProductScale> findByPluCode(String pluCode);

    /** Check whether a PLU code is already taken by a different row. */
    boolean existsByPluCodeAndIdNot(String pluCode, Long id);
}
