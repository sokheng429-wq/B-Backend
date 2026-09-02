package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.SalePromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalePromotionRepository extends JpaRepository<SalePromotion, Long> {

    Optional<SalePromotion> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT p FROM SalePromotion p WHERE " +
           "(:field = 'code' AND LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'description' AND (LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.secondLanguage) LIKE LOWER(CONCAT('%', :query, '%')))) OR " +
           "(:field = 'any' AND (LOWER(p.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(p.secondLanguage) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "ORDER BY p.createdAt DESC")
    List<SalePromotion> searchByField(@Param("field") String field, @Param("query") String query);

    List<SalePromotion> findByActiveTrueOrderByCreatedAtDesc();
}