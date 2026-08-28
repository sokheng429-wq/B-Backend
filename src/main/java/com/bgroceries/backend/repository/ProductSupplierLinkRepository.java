package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.ProductSupplierLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSupplierLinkRepository extends JpaRepository<ProductSupplierLink, Long> {

    /** All supplier links for a given product. */
    List<ProductSupplierLink> findByProductId(Long productId);

    /** All active links for a product. */
    List<ProductSupplierLink> findByProductIdAndActiveTrue(Long productId);

    /** All links for a given supplier. */
    List<ProductSupplierLink> findBySupplierId(Long supplierId);

    /** Check whether a product-supplier pair already exists. */
    boolean existsByProductIdAndSupplierId(Long productId, Long supplierId);
}
