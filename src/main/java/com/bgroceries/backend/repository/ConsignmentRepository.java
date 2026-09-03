package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.Consignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {

    Optional<Consignment> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT c FROM Consignment c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.customerPhone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.salesperson) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.reference) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR c.status = :status) AND " +
           "(:outlet IS NULL OR :outlet = '' OR :outlet = 'ALL' OR c.outlet = :outlet) AND " +
           "(:customer IS NULL OR :customer = '' OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :customer, '%'))) AND " +
           "(:salesperson IS NULL OR :salesperson = '' OR LOWER(c.salesperson) LIKE LOWER(CONCAT('%', :salesperson, '%'))) AND " +
           "(:startDate IS NULL OR c.consignmentDate >= :startDate) AND " +
           "(:endDate IS NULL OR c.consignmentDate <= :endDate) " +
           "ORDER BY c.createdAt DESC")
    List<Consignment> searchConsignments(
            @Param("search") String search,
            @Param("status") String status,
            @Param("outlet") String outlet,
            @Param("customer") String customer,
            @Param("salesperson") String salesperson,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT c FROM Consignment c WHERE " +
           "(:field = 'code' AND LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'customer' AND LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'any' AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(c.customerPhone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(c.salesperson) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "ORDER BY c.createdAt DESC")
    List<Consignment> searchByField(@Param("field") String field, @Param("query") String query);

    List<Consignment> findTop50ByOrderByCreatedAtDesc();
}
