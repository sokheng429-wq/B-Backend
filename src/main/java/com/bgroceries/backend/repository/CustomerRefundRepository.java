package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.CustomerRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRefundRepository extends JpaRepository<CustomerRefund, Long> {

    Optional<CustomerRefund> findByCode(String code);

    @Query("SELECT r FROM CustomerRefund r WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.partner) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.contact) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.phone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.username) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR r.status = :status) AND " +
           "(:startDate IS NULL OR r.paymentDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.paymentDate <= :endDate) " +
           "ORDER BY r.createdAt DESC")
    List<CustomerRefund> searchRefunds(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT r FROM CustomerRefund r WHERE " +
           "(:field = 'code' AND LOWER(r.code) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'amount' AND CAST(r.paidAmount AS string) LIKE CONCAT('%', :query, '%')) OR " +
           "(:field = 'rate' AND CAST(r.rate AS string) LIKE CONCAT('%', :query, '%')) OR " +
           "(:field = 'partner' AND LOWER(r.partner) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'contact' AND (LOWER(r.contact) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.phone) LIKE LOWER(CONCAT('%', :query, '%')))) OR " +
           "(:field = 'any' AND (LOWER(r.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                      LOWER(r.partner) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                      LOWER(r.contact) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                      LOWER(r.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                      LOWER(r.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                      CAST(r.paidAmount AS string) LIKE CONCAT('%', :query, '%') OR " +
           "                      CAST(r.rate AS string) LIKE CONCAT('%', :query, '%'))) " +
           "ORDER BY r.createdAt DESC")
    List<CustomerRefund> searchByField(@Param("field") String field, @Param("query") String query);

    List<CustomerRefund> findTop50ByOrderByCreatedAtDesc();
}
