package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT q FROM Quotation q WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(q.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(q.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(q.customerPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(q.salesperson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(q.reference) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR q.status = :status) AND " +
           "(:outlet IS NULL OR :outlet = '' OR :outlet = 'ALL' OR q.outlet = :outlet) AND " +
           "(:customer IS NULL OR :customer = '' OR LOWER(q.customerName) LIKE LOWER(CONCAT('%', :customer, '%'))) AND " +
           "(:salesperson IS NULL OR :salesperson = '' OR LOWER(q.salesperson) LIKE LOWER(CONCAT('%', :salesperson, '%'))) AND " +
           "(:startDate IS NULL OR q.quotationDate >= :startDate) AND " +
           "(:endDate IS NULL OR q.quotationDate <= :endDate) " +
           "ORDER BY q.createdAt DESC")
    List<Quotation> searchQuotations(
            @Param("search") String search,
            @Param("status") String status,
            @Param("outlet") String outlet,
            @Param("customer") String customer,
            @Param("salesperson") String salesperson,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT q FROM Quotation q WHERE " +
           "(:field = 'code' AND LOWER(q.code) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'customer' AND LOWER(q.customerName) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'any' AND (LOWER(q.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(q.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(q.customerPhone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(q.salesperson) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "ORDER BY q.createdAt DESC")
    List<Quotation> searchByField(@Param("field") String field, @Param("query") String query);

    List<Quotation> findTop50ByOrderByCreatedAtDesc();
}
