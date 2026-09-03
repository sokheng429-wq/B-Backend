package com.bgroceries.backend.repository;

import com.bgroceries.backend.entity.Sale.CustomerDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDepositRepository extends JpaRepository<CustomerDeposit, Long> {

    Optional<CustomerDeposit> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT d FROM CustomerDeposit d WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.contact) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.reference) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(d.username) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR :status = 'ALL' OR d.status = :status) AND " +
           "(:startDate IS NULL OR d.depositDate >= :startDate) AND " +
           "(:endDate IS NULL OR d.depositDate <= :endDate) " +
           "ORDER BY d.createdAt DESC")
    List<CustomerDeposit> searchDeposits(
            @Param("search") String search,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT d FROM CustomerDeposit d WHERE " +
           "(:field = 'code' AND LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "(:field = 'amount' AND CAST(d.amount AS string) LIKE CONCAT('%', :query, '%')) OR " +
           "(:field = 'any' AND (LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(d.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     LOWER(d.contact) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "                     CAST(d.amount AS string) LIKE CONCAT('%', :query, '%'))) " +
           "ORDER BY d.createdAt DESC")
    List<CustomerDeposit> searchByField(@Param("field") String field, @Param("query") String query);

    List<CustomerDeposit> findTop50ByOrderByCreatedAtDesc();
}
