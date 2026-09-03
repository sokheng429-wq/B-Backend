package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleOrderRepository extends JpaRepository<SaleOrder, Long> {
    Optional<SaleOrder> findByCode(String code);

    @Query("SELECT s FROM SaleOrder s WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.quoteCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.poCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.customerPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(s.salesperson) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR s.status = :status) AND " +
           "(:outlet IS NULL OR :outlet = '' OR :outlet = 'ALL' OR s.outlet = :outlet) AND " +
           "(:startDate IS NULL OR s.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR s.orderDate <= :endDate) " +
           "ORDER BY s.createdAt DESC")
    List<SaleOrder> searchSaleOrders(
            @Param("search") String search,
            @Param("status") String status,
            @Param("outlet") String outlet,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<SaleOrder> findTop50ByOrderByCreatedAtDesc();
}
