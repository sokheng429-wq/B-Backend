package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.ReturnInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnInvoiceRepository extends JpaRepository<ReturnInvoice, Long> {

    Optional<ReturnInvoice> findByInvoiceCode(String invoiceCode);

    boolean existsByInvoiceCode(String invoiceCode);

    @Query("SELECT r FROM ReturnInvoice r WHERE " +
           "(:field = 'invoiceCode' AND LOWER(r.invoiceCode) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'applyToInvoice' AND LOWER(r.applyToInvoice) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'customer' AND LOWER(r.customerName) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'phone' AND LOWER(r.customerPhone) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'any' AND (LOWER(r.invoiceCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(r.applyToInvoice) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(r.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(r.customerPhone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(r.soCode) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "ORDER BY r.createdAt DESC")
    List<ReturnInvoice> searchByField(@Param("field") String field, @Param("query") String query);

    @Query("SELECT r FROM ReturnInvoice r WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(r.invoiceCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.applyToInvoice) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.customerPhone) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:outlet IS NULL OR :outlet = '' OR :outlet = 'all' OR r.outlet = :outlet) AND " +
           "(:startDate IS NULL OR r.returnDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.returnDate <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    List<ReturnInvoice> searchWithFilters(
            @Param("search") String search,
            @Param("outlet") String outlet,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}