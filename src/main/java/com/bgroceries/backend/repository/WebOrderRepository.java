package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.WebOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebOrderRepository extends JpaRepository<WebOrder, Long> {
    Optional<WebOrder> findByCode(String code);

    @Query("SELECT w FROM WebOrder w WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(w.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(w.phone) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR w.status = :status) AND " +
           "(:outlet IS NULL OR :outlet = '' OR :outlet = 'ALL' OR w.outlet = :outlet) AND " +
           "(:startDate IS NULL OR w.orderDate >= :startDate) AND " +
           "(:endDate IS NULL OR w.orderDate <= :endDate) " +
           "ORDER BY w.createdAt DESC")
    List<WebOrder> searchWebOrders(
            @Param("search") String search,
            @Param("status") String status,
            @Param("outlet") String outlet,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<WebOrder> findTop50ByOrderByCreatedAtDesc();
}
