package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.SaleInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleInvoiceRepository extends JpaRepository<SaleInvoice, Long> {

    Optional<SaleInvoice> findByInvoiceCode(String invoiceCode);

    boolean existsByInvoiceCode(String invoiceCode);

    @Query("SELECT i FROM SaleInvoice i WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.soCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.customerPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(i.salesperson) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR i.status = :status) AND " +
           "(:startDate IS NULL OR i.invoiceDate >= :startDate) AND " +
           "(:endDate IS NULL OR i.invoiceDate <= :endDate) " +
           "ORDER BY i.createdAt DESC")
    List<SaleInvoice> searchInvoices(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT i FROM SaleInvoice i WHERE " +
           "(:field = 'invoiceCode' AND LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'soCode' AND LOWER(i.soCode) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'customer' AND LOWER(i.customerName) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'phone' AND LOWER(i.customerPhone) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'any' AND (LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(i.soCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(i.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(i.customerPhone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(i.salesperson) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "ORDER BY i.createdAt DESC")
    List<SaleInvoice> searchByField(@Param("field") String field, @Param("query") String query);

    List<SaleInvoice> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}